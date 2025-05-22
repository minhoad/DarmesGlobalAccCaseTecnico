package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.ExchangeRate;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.ExchangeRateRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.ExchangeRateUnavailableException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.FutureDateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BCBExchangeRateServiceTest {

    @Mock
    private BCBDirectClient bcbDirectClient;

    @Mock
    private ExchangeRateRepository repository;

    @InjectMocks
    private BCBExchangeRateService exchangeRateService;

    private final LocalDate today = LocalDate.now();
    private final LocalDate yesterday = today.minusDays(1);
    private final LocalDate tomorrow = today.plusDays(1);
    private final LocalDate lastFriday = today.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
    private final BigDecimal exchangeRate = new BigDecimal("5.25");

    @Test
    @DisplayName("Deve retornar taxa de câmbio do repositório quando disponível")
    void getExchangeRate_WithRateInRepository_ShouldReturnRate() {
        // Arrange
        ExchangeRate rate = new ExchangeRate(today, exchangeRate);
        when(repository.findByDate(today)).thenReturn(Optional.of(rate));

        // Act
        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(today);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(exchangeRate);

        // Verify
        verify(repository).findByDate(today);
        verify(bcbDirectClient, never()).getCotacaoDia(any());
    }

    @Test
    @DisplayName("Deve buscar taxa de câmbio da API do BCB quando não disponível no repositório")
    void getExchangeRate_WithoutRateInRepository_ShouldFetchFromBCB() {
        // Arrange
        when(repository.findByDate(today)).thenReturn(Optional.empty());

        BCBClient.CotacaoResponse.Cotacao cotacao = mock(BCBClient.CotacaoResponse.Cotacao.class);
        when(cotacao.compra()).thenReturn(exchangeRate);
        when(cotacao.data()).thenReturn(today.atStartOfDay());

        BCBClient.CotacaoResponse response = mock(BCBClient.CotacaoResponse.class);
        when(response.cotacoes()).thenReturn(Collections.singletonList(cotacao));

        when(bcbDirectClient.getCotacaoDia(today)).thenReturn(response);

        ExchangeRate savedRate = new ExchangeRate(today, exchangeRate);
        when(repository.save(any(ExchangeRate.class))).thenReturn(savedRate);

        // Act
        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(today);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(exchangeRate);

        // Verify
        verify(repository, atLeastOnce()).findByDate(today);
        verify(bcbDirectClient).getCotacaoDia(today);
        verify(repository).save(any(ExchangeRate.class));
    }

    @Test
    @DisplayName("Deve lançar FutureDateException para datas futuras")
    void getExchangeRate_WithFutureDate_ShouldThrowFutureDateException() {
        // Act & Assert
        assertThatThrownBy(() -> exchangeRateService.getExchangeRate(tomorrow))
                .isInstanceOf(FutureDateException.class);

        // Verify
        verify(repository, never()).findByDate(any());
        verify(bcbDirectClient, never()).getCotacaoDia(any());
    }

    @Test
    @DisplayName("Deve buscar última taxa disponível para fins de semana")
    void getExchangeRate_WithWeekendDate_ShouldReturnLastWeekdayRate() {
        // Arrange
        LocalDate saturday = today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        LocalDate friday = saturday.minusDays(1);

        ExchangeRate fridayRate = new ExchangeRate(friday, exchangeRate);
        when(repository.findLatestUntilDate(friday)).thenReturn(Optional.of(fridayRate));

        // Act
        Optional<BigDecimal> result = exchangeRateService.getExchangeRate(saturday);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(exchangeRate);

        // Verify
        verify(repository).findLatestUntilDate(friday);
        verify(bcbDirectClient, never()).getCotacaoDia(any());
    }

    @Test
    @DisplayName("Deve usar fallback quando a API do BCB falha")
    void getExchangeRate_WithBCBFailure_ShouldUseFallback() {

        ExchangeRate lastRate = new ExchangeRate(yesterday, exchangeRate);
        when(repository.findLatestValidRateBefore(today)).thenReturn(Optional.of(lastRate));

        // Act
        Optional<BigDecimal> result = exchangeRateService.getLastAvailableRate(today, new ExchangeRateUnavailableException());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(exchangeRate);

        // Verify
        verify(repository).findLatestValidRateBefore(today);
    }
}
