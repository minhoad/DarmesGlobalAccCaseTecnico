package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.Transaction;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPF;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPJ;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.enums.Currency;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.TransactionRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.balance.BalanceManagementService;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.BusinessException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.ExchangeRateUnavailableException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.DailyLimitExceededException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.InsufficientBalanceException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.InvalidAmountException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.SelfTransferException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RemittanceService {
    private static final MathContext DIVISION_CONTEXT = new MathContext(10, RoundingMode.HALF_EVEN);

    private final UserQueryService userQueryService;
    private final BalanceManagementService balanceManagementService;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateStrategy exchangeRateStrategy;
    private final Clock clock;

    public Transaction executeRemittance(Long senderId, Long receiverId, BigDecimal amountBRL) {
        validateInput(senderId, receiverId, amountBRL);

        User sender = userQueryService.findEntityById(senderId);
        User receiver = userQueryService.findEntityById(receiverId);

        validateBalance(senderId, amountBRL);
        validateDailyLimit(sender, amountBRL);

        BigDecimal exchangeRate = getExchangeRate();
        BigDecimal amountUSD = convertCurrency(amountBRL, exchangeRate);

        updateBalances(senderId, receiverId, amountBRL, amountUSD);

        return createTransaction(sender, receiver, amountBRL, amountUSD, exchangeRate);
    }

    private void validateInput(Long senderId, Long receiverId, BigDecimal amount) {
        if (senderId == null || receiverId == null || amount == null) {
            throw new IllegalArgumentException("Parâmetros inválidos");
        }

        if (senderId.equals(receiverId)) {
            throw new SelfTransferException(); // Exceção específica
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(); // Exceção específica
        }
    }

    private void validateBalance(Long userId, BigDecimal amount) {
        BigDecimal balance = balanceManagementService.getBalance(userId, Currency.BRL);
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
    }

    private void validateDailyLimit(User sender, BigDecimal amount) {
        LocalDate today = LocalDate.now(clock);
        BigDecimal dailySpent = getDailyTransactionTotal(sender, today);

        BigDecimal remainingLimit = sender.getDailyLimit().subtract(dailySpent);
        if (amount.compareTo(remainingLimit) > 0) {
            throw new DailyLimitExceededException(sender.getDailyLimit());
        }
    }
    @Cacheable(value = "dailyTransactions", key = "#user.id + '_' + #date")
    public BigDecimal getDailyTransactionTotal(User user, LocalDate date) {
        log.info("Calculating daily transaction total for user {} on date {} (not from cache)",
                user.getId(), date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        return transactionRepository.calculateDailyTotal(user, startOfDay, endOfDay);
    }

    private BigDecimal getExchangeRate() {
        return exchangeRateStrategy.getExchangeRate(LocalDate.now(clock))
                .orElseThrow(() -> new ExchangeRateUnavailableException(LocalDate.now(clock)));
    }

    private BigDecimal convertCurrency(BigDecimal amountBRL, BigDecimal exchangeRate) {
        return amountBRL.divide(exchangeRate, DIVISION_CONTEXT)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    private void updateBalances(Long senderId, Long receiverId,
                                BigDecimal amountBRL, BigDecimal amountUSD) {
        balanceManagementService.deductBalance(senderId, amountBRL, Currency.BRL);
        balanceManagementService.addBalance(receiverId, amountUSD, Currency.USD);
    }

    private Transaction createTransaction(User sender, User receiver,
                                          BigDecimal amountBRL, BigDecimal amountUSD,
                                          BigDecimal exchangeRate) {
        return transactionRepository.save(Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amountBRL(amountBRL)
                .amountUSD(amountUSD)
                .exchangeRate(exchangeRate)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }
}