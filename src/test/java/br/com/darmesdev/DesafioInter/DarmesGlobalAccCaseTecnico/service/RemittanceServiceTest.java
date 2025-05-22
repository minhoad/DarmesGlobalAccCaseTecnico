package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.Transaction;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPF;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPJ;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.enums.Currency;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.TransactionRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.balance.BalanceManagementService;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.DailyLimitExceededException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.InsufficientBalanceException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.InvalidAmountException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.SelfTransferException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.query.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RemittanceServiceTest {

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private BalanceManagementService balanceManagementService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ExchangeRateStrategy exchangeRateStrategy;

    @Mock
    private Clock clock;

    @InjectMocks
    private RemittanceService remittanceService;

    private UserPF sender;
    private UserPJ receiver;
    private final LocalDate today = LocalDate.of(2025, 5, 21);
    private final BigDecimal exchangeRate = new BigDecimal("5.00");
    private final BigDecimal amountBRL = new BigDecimal("100.00");
    private final BigDecimal amountUSD = new BigDecimal("20.00");

    @BeforeEach
    void setUp() {
        // Configurar usuários de teste
        sender = new UserPF();
        sender.setId(1L);
        sender.setFullName("Remetente"); // Usando o nome correto do campo
        sender.setEmail("remetente@example.com");
        sender.setBalanceBRL(new BigDecimal("1000.00"));
        sender.setCpf("782.883.370-90");

        receiver = new UserPJ();
        receiver.setId(2L);
        receiver.setFullName("Destinatário"); // Usando o nome correto do campo
        receiver.setEmail("destinatario@example.com");
        receiver.setBalanceUSD(new BigDecimal("100.00"));
        receiver.setCnpj("91.041.096/0001-91");

    }

    @Test
    @DisplayName("Deve executar remessa com sucesso quando todos os parâmetros são válidos")
    void executeRemittance_WithValidParameters_ShouldSucceed() {
        // Configurar o relógio mockado para retornar uma data fixa
        Clock fixedClock = Clock.fixed(
                today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
// Configurar comportamento padrão dos mocks
        when(userQueryService.findEntityById(1L)).thenReturn(sender);
        when(userQueryService.findEntityById(2L)).thenReturn(receiver);
        when(exchangeRateStrategy.getExchangeRate(any(LocalDate.class))).thenReturn(java.util.Optional.of(exchangeRate));
        when(transactionRepository.calculateDailyTotal(eq(sender), any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(balanceManagementService.getBalance(1L, Currency.BRL)).thenReturn(sender.getBalanceBRL());

        // Act
        Transaction result = remittanceService.executeRemittance(1L, 2L, amountBRL);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSender()).isEqualTo(sender);
        assertThat(result.getReceiver()).isEqualTo(receiver);
        assertThat(result.getAmountBRL()).isEqualByComparingTo(amountBRL);
        assertThat(result.getAmountUSD()).isEqualByComparingTo(amountUSD);
        assertThat(result.getExchangeRate()).isEqualByComparingTo(exchangeRate);

        // Verify
        verify(balanceManagementService).deductBalance(1L, amountBRL, Currency.BRL);
        verify(balanceManagementService).addBalance(2L, amountUSD, Currency.USD);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Deve lançar SelfTransferException quando remetente e destinatário são iguais")
    void executeRemittance_WithSameUserIds_ShouldThrowSelfTransferException() {
        // Arrange
        Long sameUserId = 1L;

        // Act & Assert
        assertThatThrownBy(() -> remittanceService.executeRemittance(sameUserId, sameUserId, amountBRL))
                .isInstanceOf(SelfTransferException.class);

        // Verify - using never() to ensure these methods are not called
        verify(balanceManagementService, never()).deductBalance(anyLong(), any(BigDecimal.class), any(Currency.class));
        verify(balanceManagementService, never()).addBalance(anyLong(), any(BigDecimal.class), any(Currency.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidAmountException quando o valor é zero ou negativo")
    void executeRemittance_WithInvalidAmount_ShouldThrowInvalidAmountException() {
        // Act & Assert
        assertThatThrownBy(() -> remittanceService.executeRemittance(1L, 2L, BigDecimal.ZERO))
                .isInstanceOf(InvalidAmountException.class);

        assertThatThrownBy(() -> remittanceService.executeRemittance(1L, 2L, new BigDecimal("-10.00")))
                .isInstanceOf(InvalidAmountException.class);

        // Verify
        verify(balanceManagementService, never()).deductBalance(anyLong(), any(), any());
        verify(balanceManagementService, never()).addBalance(anyLong(), any(), any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Deve lançar InsufficientBalanceException quando o saldo é insuficiente")
    void executeRemittance_WithInsufficientBalance_ShouldThrowInsufficientBalanceException() {
        // Arrange
        when(balanceManagementService.getBalance(1L, Currency.BRL)).thenReturn(new BigDecimal("50.00"));

        // Act & Assert
        assertThatThrownBy(() -> remittanceService.executeRemittance(1L, 2L, amountBRL))
                .isInstanceOf(InsufficientBalanceException.class);

        // Verify
        verify(balanceManagementService, never()).deductBalance(anyLong(), any(), any());
        verify(balanceManagementService, never()).addBalance(anyLong(), any(), any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Deve lançar DailyLimitExceededException quando o limite diário é excedido")
    void executeRemittance_WithDailyLimitExceeded_ShouldThrowDailyLimitExceededException() {
        // Valor acumulado + novo valor deve exceder o limite
        when(transactionRepository.calculateDailyTotal(any(), any(), any()))
                .thenReturn(new BigDecimal("9500.00")); // PF: 9500 + 500 = 10000 (limite)

        assertThatThrownBy(() -> remittanceService.executeRemittance(1L, 2L, new BigDecimal("500.00")))
                .isInstanceOf(DailyLimitExceededException.class);
    }
}
