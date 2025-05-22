package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.integration;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.ExchangeRate;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.ExchangeRateRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.BCBClient;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.BCBDirectClient;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.BCBExchangeRateService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BCBExchangeRateIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private BCBExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("api.bcb.base-url", () -> "http://localhost:" + wireMockServer.port());
    }

    @Test
    @DisplayName("Deve buscar taxa de câmbio da API do BCB e salvar no repositório")
    void getExchangeRate_ShouldFetchFromBCBAndSave() {
        // Arrange
        LocalDate today = LocalDate.now();
        String formattedDate = String.format("%02d-%02d-%d", today.getMonthValue(), today.getDayOfMonth(), today.getYear());

        String responseBody = """
            {
              "value": [
                {
                  "cotacaoCompra": 5.25,
                  "cotacaoVenda": 5.26,
                  "dataHoraCotacao": "%s 13:00:00.000"
                }
              ]
            }
            """.formatted(today);

        stubFor(get(urlPathMatching("/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseBody)));

        when(exchangeRateRepository.findByDate(any())).thenReturn(Optional.empty());
        when(exchangeRateRepository.save(any())).thenAnswer(invocation -> {
            ExchangeRate rate = invocation.getArgument(0);
            return rate;
        });

        // Act
        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(today);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(new BigDecimal("5.25"));

        verify(getRequestedFor(urlPathMatching("/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia.*"))
                .withQueryParam("@dataCotacao", containing(formattedDate)));
    }

    @Test
    @DisplayName("Deve usar fallback quando a API do BCB retorna erro")
    void getExchangeRate_WhenBCBReturnsError_ShouldUseFallback() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        stubFor(get(urlPathMatching("/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia.*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        when(exchangeRateRepository.findByDate(any())).thenReturn(Optional.empty());

        ExchangeRate fallbackRate = new ExchangeRate(yesterday, new BigDecimal("5.20"));
        when(exchangeRateRepository.findLatestValidRateBefore(any())).thenReturn(Optional.of(fallbackRate));

        // Act
        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(today);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(new BigDecimal("5.20"));
    }
}
