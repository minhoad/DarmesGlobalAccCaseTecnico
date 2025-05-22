package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.Transaction;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPF;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPJ;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.enums.Currency;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.TransactionRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.balance.BalanceManagementService;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.DailyLimitExceededException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.query.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class TransactionLimitsTest {

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

    private UserPF pfSender;
    private UserPJ pjSender;
    private User receiver;
    private final LocalDate today = LocalDate.of(2025, 5, 21);
    private final BigDecimal exchangeRate = new BigDecimal("5.00");

    @BeforeEach
    void setUp() {
        // Configurar o relógio mockado para retornar uma data fixa
        Clock fixedClock = Clock.fixed(
                today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        // Configurar usuários de teste
        pfSender = new UserPF();
        pfSender.setId(1L);
        pfSender.setFullName("Remetente PF");
        pfSender.setEmail("pf@example.com");
        pfSender.setBalanceBRL(new BigDecimal("20000.00"));
        pfSender.setCpf("123.456.789-00");

        pjSender = new UserPJ();
        pjSender.setId(2L);
        pjSender.setFullName("Remetente PJ");
        pjSender.setEmail("pj@example.com");
        pjSender.setBalanceBRL(new BigDecimal("100000.00"));
        pjSender.setCnpj("12.345.678/0001-90");

        receiver = new UserPF();
        receiver.setId(3L);
        receiver.setFullName("Destinatário");
        receiver.setEmail("destinatario@example.com");
        receiver.setBalanceUSD(new BigDecimal("1000.00"));


    }

    @Test
    @DisplayName("Deve permitir remessa PF dentro do limite diário")
    void executeRemittance_PFWithinDailyLimit_ShouldSucceed() {
        // Arrange
        BigDecimal amountBRL = new BigDecimal("9000.00");

        when(userQueryService.findEntityById(1L)).thenReturn(pfSender);
        when(userQueryService.findEntityById(3L)).thenReturn(receiver);
        when(transactionRepository.calculateDailyTotal(eq(pfSender), any(), any())).thenReturn(new BigDecimal("0.00"));
        when(balanceManagementService.getBalance(1L, Currency.BRL)).thenReturn(pfSender.getBalanceBRL());

        // Act
        Transaction result = remittanceService.executeRemittance(1L, 3L, amountBRL);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSender()).isEqualTo(pfSender);
        assertThat(result.getReceiver()).isEqualTo(receiver);
        assertThat(result.getAmountBRL()).isEqualByComparingTo(amountBRL);

        // Verify
        verify(balanceManagementService).deductBalance(1L, amountBRL, Currency.BRL);
        verify(balanceManagementService).addBalance(
                3L,
                amountBRL.divide(exchangeRate, 2, RoundingMode.HALF_EVEN),
                Currency.USD
        );
        // Configurar comportamento padrão dos mocks
        when(exchangeRateStrategy.getExchangeRate(any(LocalDate.class))).thenReturn(java.util.Optional.of(exchangeRate));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Deve lançar DailyLimitExceededException quando PF excede limite diário")
    void executeRemittance_PFExceedingDailyLimit_ShouldThrowException() {
        // Arrange
        BigDecimal amountBRL = new BigDecimal("5000.00");

        when(userQueryService.findEntityById(1L)).thenReturn(pfSender);
        when(userQueryService.findEntityById(3L)).thenReturn(receiver);
        when(transactionRepository.calculateDailyTotal(eq(pfSender), any(), any())).thenReturn(new BigDecimal("6000.00"));
        when(balanceManagementService.getBalance(1L, Currency.BRL)).thenReturn(pfSender.getBalanceBRL());

        // Act & Assert
        assertThatThrownBy(() -> remittanceService.executeRemittance(1L, 3L, amountBRL))
                .isInstanceOf(DailyLimitExceededException.class);

        // Verify
        verify(balanceManagementService, never()).deductBalance(anyLong(), any(), any());
        verify(balanceManagementService, never()).addBalance(anyLong(), any(), any());
    }

    @Test
    @DisplayName("Deve permitir remessa PJ dentro do limite diário")
    void executeRemittance_PJWithinDailyLimit_ShouldSucceed() {
        // Arrange
        BigDecimal amountBRL = new BigDecimal("45000.00");

        when(userQueryService.findEntityById(2L)).thenReturn(pjSender);
        when(userQueryService.findEntityById(3L)).thenReturn(receiver);
        when(transactionRepository.calculateDailyTotal(eq(pjSender), any(), any())).thenReturn(new BigDecimal("0.00"));
        when(balanceManagementService.getBalance(2L, Currency.BRL)).thenReturn(pjSender.getBalanceBRL());

        // Act
        Transaction result = remittanceService.executeRemittance(2L, 3L, amountBRL);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSender()).isEqualTo(pjSender);
        assertThat(result.getReceiver()).isEqualTo(receiver);
        assertThat(result.getAmountBRL()).isEqualByComparingTo(amountBRL);

        // Verify
        verify(balanceManagementService).deductBalance(2L, amountBRL, Currency.BRL);
        verify(balanceManagementService).addBalance(
                3L,
                amountBRL.divide(exchangeRate, 2, RoundingMode.HALF_EVEN),
                Currency.USD
        );
    }

    @Test
    @DisplayName("Deve lançar DailyLimitExceededException quando PJ excede limite diário")
    void executeRemittance_PJExceedingDailyLimit_ShouldThrowException() {
        // Arrange
        BigDecimal amountBRL = new BigDecimal("10000.00");

        when(userQueryService.findEntityById(2L)).thenReturn(pjSender);
        when(userQueryService.findEntityById(3L)).thenReturn(receiver);
        when(transactionRepository.calculateDailyTotal(eq(pjSender), any(), any())).thenReturn(new BigDecimal("45000.00"));
        when(balanceManagementService.getBalance(2L, Currency.BRL)).thenReturn(pjSender.getBalanceBRL());

        // Act & Assert
        assertThatThrownBy(() -> remittanceService.executeRemittance(2L, 3L, amountBRL))
                .isInstanceOf(DailyLimitExceededException.class);

        // Verify
        verify(balanceManagementService, never()).deductBalance(anyLong(), any(), any());
        verify(balanceManagementService, never()).addBalance(anyLong(), any(), any());
    }
}
