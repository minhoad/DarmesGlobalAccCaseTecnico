package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.integration;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.RemittanceRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.ExchangeRate;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPF;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RemittanceIntegrationTest {

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

    private User sender;
    private User receiver;
    private ExchangeRate exchangeRate;

    @BeforeEach
    void setUp() {
        // Limpar dados de teste anteriores
        transactionRepository.deleteAll();
        userRepository.deleteAll();
        exchangeRateRepository.deleteAll();

        // Criar usuários de teste
        sender = new UserPF();
        sender.setId(1L);
        sender.setFullName("Remetente Teste");
        sender.setEmail("remetente@teste.com");
        sender.setPassword("senha123");
        sender.setBalanceBRL(new BigDecimal("1000.00"));
        sender.setBalanceUSD(BigDecimal.ZERO);
        sender.setVersion(1L);
        sender = userRepository.save(sender);

        receiver = new UserPF();
        receiver.setId(2L);
        receiver.setFullName("Destinatário Teste");
        receiver.setEmail("destinatario@teste.com");
        receiver.setPassword("senha123");
        receiver.setBalanceBRL(BigDecimal.ZERO);
        receiver.setBalanceUSD(new BigDecimal("100.00"));
        receiver.setVersion(1L);
        receiver = userRepository.save(receiver);

        // Criar taxa de câmbio
        exchangeRate = new ExchangeRate(LocalDate.now(), new BigDecimal("5.00"));
        exchangeRate = exchangeRateRepository.save(exchangeRate);

        // Mock do BCBDirectClient para não chamar a API real
        when(bcbDirectClient.getCotacaoDia(any())).thenThrow(new RuntimeException("Não deve chamar a API real durante testes"));
    }

    @Test
    @DisplayName("Deve executar uma remessa com sucesso")
    void executeRemittance_WithValidRequest_ShouldSucceed() throws Exception {
        // Arrange
        RemittanceRequest request = new RemittanceRequest(
                sender.getId(),
                receiver.getId(),
                new BigDecimal("100.00")
        );

        // Act & Assert
        mockMvc.perform(post("/remittances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderId").value(sender.getId()))
                .andExpect(jsonPath("$.receiverId").value(receiver.getId()))
                .andExpect(jsonPath("$.amountBRL").value("100.00"))
                .andExpect(jsonPath("$.amountUSD").value("20.00"))
                .andExpect(jsonPath("$.exchangeRate").value("5.00"));

        // Verificar se os saldos foram atualizados
        User updatedSender = userRepository.findById(sender.getId()).orElseThrow();
        User updatedReceiver = userRepository.findById(receiver.getId()).orElseThrow();

        assertThat(updatedSender.getBalanceBRL()).isEqualByComparingTo(new BigDecimal("900.00"));
        assertThat(updatedReceiver.getBalanceUSD()).isEqualByComparingTo(new BigDecimal("120.00"));
    }

    @Test
    @DisplayName("Deve falhar quando o saldo é insuficiente")
    void executeRemittance_WithInsufficientBalance_ShouldFail() throws Exception {
        // Arrange
        RemittanceRequest request = new RemittanceRequest(
                sender.getId(),
                receiver.getId(),
                new BigDecimal("2000.00")
        );

        // Act & Assert
        mockMvc.perform(post("/remittances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verificar se os saldos não foram alterados
        User updatedSender = userRepository.findById(sender.getId()).orElseThrow();
        User updatedReceiver = userRepository.findById(receiver.getId()).orElseThrow();

        assertThat(updatedSender.getBalanceBRL()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(updatedReceiver.getBalanceUSD()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
