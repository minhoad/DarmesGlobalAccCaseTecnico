package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.balance.impl;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPF;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.enums.Currency;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.UserRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.query.UserNotFoundException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.InsufficientBalanceException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BalanceManagementServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BalanceManagementServiceImpl balanceService;

    private User user;
    private final Long userId = 1L;
    private final BigDecimal initialBalanceBRL = new BigDecimal("1000.00");
    private final BigDecimal initialBalanceUSD = new BigDecimal("200.00");

    @BeforeEach
    void setUp() {
        user = new UserPF();
        user.setId(userId);
        user.setFullName("Usuário Teste"); // Usando o nome correto do campo
        user.setEmail("usuario@teste.com");
        user.setBalanceBRL(initialBalanceBRL);
        user.setBalanceUSD(initialBalanceUSD);
        user.setVersion(0L);

        // Configurar o mock para ser lenient (não estrito)
        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Deve retornar o saldo em BRL corretamente")
    void getBalance_WithBRLCurrency_ShouldReturnBRLBalance() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        BigDecimal result = balanceService.getBalance(userId, Currency.BRL);

        // Assert
        assertThat(result).isEqualByComparingTo(initialBalanceBRL);

        // Verify
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Deve retornar o saldo em USD corretamente")
    void getBalance_WithUSDCurrency_ShouldReturnUSDBalance() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        BigDecimal result = balanceService.getBalance(userId, Currency.USD);

        // Assert
        assertThat(result).isEqualByComparingTo(initialBalanceUSD);

        // Verify
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando usuário não existe")
    void getBalance_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> balanceService.getBalance(999L, Currency.BRL))
                .isInstanceOf(UserNotFoundException.class);

        // Verify
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve adicionar saldo em BRL corretamente")
    void addBalance_WithBRLCurrency_ShouldAddToBRLBalance() {
        // Arrange
        BigDecimal amountToAdd = new BigDecimal("500.00");
        BigDecimal expectedBalance = initialBalanceBRL.add(amountToAdd);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        balanceService.addBalance(userId, amountToAdd, Currency.BRL);

        // Assert
        assertThat(user.getBalanceBRL()).isEqualByComparingTo(expectedBalance);

        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Deve adicionar saldo em USD corretamente")
    void addBalance_WithUSDCurrency_ShouldAddToUSDBalance() {
        // Arrange
        BigDecimal amountToAdd = new BigDecimal("100.00");
        BigDecimal expectedBalance = initialBalanceUSD.add(amountToAdd);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        balanceService.addBalance(userId, amountToAdd, Currency.USD);

        // Assert
        assertThat(user.getBalanceUSD()).isEqualByComparingTo(expectedBalance);

        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Deve lançar InvalidAmountException ao adicionar valor zero ou negativo")
    void addBalance_WithInvalidAmount_ShouldThrowInvalidAmountException() {
        // Act & Assert
        assertThatThrownBy(() -> balanceService.addBalance(userId, BigDecimal.ZERO, Currency.BRL))
                .isInstanceOf(InvalidAmountException.class);

        assertThatThrownBy(() -> balanceService.addBalance(userId, new BigDecimal("-10.00"), Currency.BRL))
                .isInstanceOf(InvalidAmountException.class);

        // Verify
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deduzir saldo em BRL corretamente")
    void deductBalance_WithBRLCurrency_ShouldDeductFromBRLBalance() {
        // Arrange
        BigDecimal amountToDeduct = new BigDecimal("300.00");
        BigDecimal expectedBalance = initialBalanceBRL.subtract(amountToDeduct);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        balanceService.deductBalance(userId, amountToDeduct, Currency.BRL);

        // Assert
        assertThat(user.getBalanceBRL()).isEqualByComparingTo(expectedBalance);

        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(userRepository, times(2)).findById(userId); // Agora espera 2 chamadas
    }

    @Test
    @DisplayName("Deve lançar InsufficientBalanceException quando saldo é insuficiente")
    void deductBalance_WithInsufficientBalance_ShouldThrowInsufficientBalanceException() {
        // Arrange
        BigDecimal amountToDeduct = new BigDecimal("1500.00");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> balanceService.deductBalance(userId, amountToDeduct, Currency.BRL))
                .isInstanceOf(InsufficientBalanceException.class);

        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve incrementar a versão do usuário ao deduzir saldo")
    void deductBalance_ShouldIncrementUserVersion() {
        // Arrange
        BigDecimal amountToDeduct = new BigDecimal("300.00");
        long initialVersion = user.getVersion();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        balanceService.deductBalance(userId, amountToDeduct, Currency.BRL);

        // Assert
        assertThat(user.getVersion()).isEqualTo(initialVersion + 1);

        // Verify
        verify(userRepository).save(user);
    }
}
