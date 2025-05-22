package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.integration;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.RemittanceRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.ExchangeRate;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPF;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPJ;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.ExchangeRateRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.TransactionRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.UserRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.BCBDirectClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CrossUserTypeRemittanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private BCBDirectClient bcbDirectClient;

    private UserPF userPF;
    private UserPJ userPJ;
    private ExchangeRate exchangeRate;

    @BeforeEach
    void setUp() {
        // Limpar dados de teste anteriores
        transactionRepository.deleteAll();
        userRepository.deleteAll();
        exchangeRateRepository.deleteAll();

        // Criar usuários de teste
        userPF = new UserPF();
        userPF.setFullName("Usuário PF");
        userPF.setEmail("pf@teste.com");
        userPF.setPassword("senha123");
        userPF.setCpf("031.465.650-21");
        userPF.setBalanceBRL(new BigDecimal("5000.00"));
        userPF.setBalanceUSD(BigDecimal.ZERO);
        userPF.setVersion(1L);
        userPF = userRepository.save(userPF);

        userPJ = new UserPJ();
        userPJ.setFullName("Empresa PJ");
        userPJ.setEmail("pj@teste.com");
        userPJ.setPassword("senha123");
        userPJ.setCnpj("39.601.207/0001-57");
        userPJ.setBalanceBRL(new BigDecimal("20000.00"));
        userPJ.setBalanceUSD(new BigDecimal("1000.00"));
        userPJ.setVersion(1L);
        userPJ = userRepository.save(userPJ);

        // Criar taxa de câmbio
        exchangeRate = new ExchangeRate(LocalDate.now(), new BigDecimal("5.00"));
        exchangeRate = exchangeRateRepository.save(exchangeRate);

        // Mock do BCBDirectClient para não chamar a API real
        when(bcbDirectClient.getCotacaoDia(any())).thenThrow(new RuntimeException("Não deve chamar a API real durante testes"));
    }

    @Test
    @DisplayName("Deve permitir remessa de PF para PJ")
    void executeRemittance_FromPFToPJ_ShouldSucceed() throws Exception {
        // Arrange
        BigDecimal amountBRL = new BigDecimal("1000.00");
        BigDecimal expectedAmountUSD = new BigDecimal("200.00");

        RemittanceRequest request = new RemittanceRequest(
                userPF.getId(),
                userPJ.getId(),
                amountBRL
        );

        // Act & Assert
        mockMvc.perform(post("/remittances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderId").value(userPF.getId()))
                .andExpect(jsonPath("$.receiverId").value(userPJ.getId()))
                .andExpect(jsonPath("$.amountBRL").value("1000.00"))
                .andExpect(jsonPath("$.amountUSD").value("200.00"));

        // Verificar se os saldos foram atualizados corretamente
        UserPF updatedPF = (UserPF) userRepository.findById(userPF.getId()).orElseThrow();
        UserPJ updatedPJ = (UserPJ) userRepository.findById(userPJ.getId()).orElseThrow();

        assertThat(updatedPF.getBalanceBRL()).isEqualByComparingTo(new BigDecimal("4000.00"));
        assertThat(updatedPJ.getBalanceUSD()).isEqualByComparingTo(new BigDecimal("1200.00"));
    }

    @Test
    @DisplayName("Deve permitir remessa de PJ para PF")
    void executeRemittance_FromPJToPF_ShouldSucceed() throws Exception {
        // Arrange
        BigDecimal amountBRL = new BigDecimal("2000.00");
        BigDecimal expectedAmountUSD = new BigDecimal("400.00");

        RemittanceRequest request = new RemittanceRequest(
                userPJ.getId(),
                userPF.getId(),
                amountBRL
        );

        // Act & Assert
        mockMvc.perform(post("/remittances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderId").value(userPJ.getId()))
                .andExpect(jsonPath("$.receiverId").value(userPF.getId()))
                .andExpect(jsonPath("$.amountBRL").value("2000.00"))
                .andExpect(jsonPath("$.amountUSD").value("400.00"));

        // Verificar se os saldos foram atualizados corretamente
        UserPF updatedPF = (UserPF) userRepository.findById(userPF.getId()).orElseThrow();
        UserPJ updatedPJ = (UserPJ) userRepository.findById(userPJ.getId()).orElseThrow();

        assertThat(updatedPJ.getBalanceBRL()).isEqualByComparingTo(new BigDecimal("18000.00"));
        assertThat(updatedPF.getBalanceUSD()).isEqualByComparingTo(new BigDecimal("400.00"));
    }

    @Test
    @DisplayName("Deve permitir múltiplas remessas entre PF e PJ dentro dos limites diários")
    void executeMultipleRemittances_BetweenPFAndPJ_ShouldSucceed() throws Exception {
        // Arrange - Primeira remessa: PF -> PJ
        RemittanceRequest request1 = new RemittanceRequest(
                userPF.getId(),
                userPJ.getId(),
                new BigDecimal("1000.00")
        );

        // Act & Assert - Primeira remessa
        mockMvc.perform(post("/remittances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // Arrange - Segunda remessa: PJ -> PF
        RemittanceRequest request2 = new RemittanceRequest(
                userPJ.getId(),
                userPF.getId(),
                new BigDecimal("1500.00")
        );

        // Act & Assert - Segunda remessa
        mockMvc.perform(post("/remittances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        // Verificar saldos finais
        UserPF updatedPF = (UserPF) userRepository.findById(userPF.getId()).orElseThrow();
        UserPJ updatedPJ = (UserPJ) userRepository.findById(userPJ.getId()).orElseThrow();

        // PF: 5000 - 1000 + 0 (BRL) / 0 + 0 + 300 (USD)
        // PJ: 20000 - 1500 + 0 (BRL) / 1000 + 200 + 0 (USD)
        assertThat(updatedPF.getBalanceBRL()).isEqualByComparingTo(new BigDecimal("4000.00"));
        assertThat(updatedPF.getBalanceUSD()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(updatedPJ.getBalanceBRL()).isEqualByComparingTo(new BigDecimal("18500.00"));
        assertThat(updatedPJ.getBalanceUSD()).isEqualByComparingTo(new BigDecimal("1200.00"));

        // Verificar transações registradas
        assertThat(transactionRepository.count()).isEqualTo(2);
    }
}
