package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.controller;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.RemittanceRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.Transaction;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPF;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.RemittanceService;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.InsufficientBalanceException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.config.TestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RemittanceController.class)
@Import(TestConfig.class)
public class RemittanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RemittanceService remittanceService;

    @Test
    @DisplayName("Deve criar uma remessa com sucesso")
    void createRemittance_WithValidRequest_ShouldReturnCreatedTransaction() throws Exception {
        // Arrange
        RemittanceRequest request = new RemittanceRequest(1L, 2L, new BigDecimal("100.00"));

        User sender = new UserPF();
        sender.setId(1L);
        sender.setPassword("12345678");
        sender.setVersion(1L);
        sender.setFullName("Rogerio Damata Alves");
        sender.setCreatedAt(LocalDateTime.now());
        sender.setEmail("contato@greenmarket.com");
        sender.setBalanceUSD(BigDecimal.valueOf(1000.00));
        sender.setBalanceBRL(BigDecimal.valueOf(1000.00));

        User receiver = new UserPF();
        receiver.setId(2L);
        receiver.setPassword("12345678");
        receiver.setVersion(1L);
        receiver.setFullName("Lucio Damata Alves");
        receiver.setCreatedAt(LocalDateTime.now());
        receiver.setEmail("engenharia@inova.com");
        receiver.setBalanceUSD(BigDecimal.valueOf(1000.00));
        receiver.setBalanceBRL(BigDecimal.valueOf(1000.00));

        Transaction transaction = Transaction.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .amountBRL(new BigDecimal("100.00"))
                .amountUSD(new BigDecimal("20.00"))
                .exchangeRate(new BigDecimal("5.00"))
                .timestamp(LocalDateTime.now())
                .build();

        when(remittanceService.executeRemittance(eq(1L), eq(2L), any(BigDecimal.class)))
                .thenReturn(transaction);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/remittances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.senderId").value(1L))
                .andExpect(jsonPath("$.receiverId").value(2L))
                .andExpect(jsonPath("$.amountBRL").value("100.00"))
                .andExpect(jsonPath("$.amountUSD").value("20.00"))
                .andExpect(jsonPath("$.exchangeRate").value("5.00"));
    }

    @Test
    @DisplayName("Deve retornar erro quando o saldo é insuficiente")
    void createRemittance_WithInsufficientBalance_ShouldReturnBadRequest() throws Exception {
        // Arrange
        RemittanceRequest request = new RemittanceRequest(1L, 2L, new BigDecimal("10000.00"));

        when(remittanceService.executeRemittance(eq(1L), eq(2L), any(BigDecimal.class)))
                .thenThrow(new InsufficientBalanceException());

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/remittances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
