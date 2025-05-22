package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.balance.impl;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.enums.Currency;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.UserRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.balance.BalanceManagementService;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.query.UserNotFoundException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.InsufficientBalanceException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.InvalidAmountException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;



@Service
@RequiredArgsConstructor
public class BalanceManagementServiceImpl implements BalanceManagementService {

    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "balances", key = "#userId + '_' + #currency")
    public BigDecimal getBalance(Long userId, Currency currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return switch (currency) {
            case BRL -> user.getBalanceBRL();
            case USD -> user.getBalanceUSD();
        };
    }

    @Transactional
    @Override
    @CacheEvict(value = "balances", key = "#userId + '_' + #currency")
    public void addBalance(Long userId, BigDecimal amount, Currency currency) {
        validateAmount(amount);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        switch (currency) {
            case BRL -> user.setBalanceBRL(user.getBalanceBRL().add(amount));
            case USD -> user.setBalanceUSD(user.getBalanceUSD().add(amount));
        }

        userRepository.save(user);
    }

    @Transactional
    @Override
    @CacheEvict(value = "balances", key = "#userId + '_' + #currency")
    public void deductBalance(Long userId, BigDecimal amount, Currency currency)
            throws InsufficientBalanceException {

        validateAmount(amount);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setVersion(user.getVersion() + 1);

        BigDecimal currentBalance = getBalance(userId, currency);

        if (currentBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        switch (currency) {
            case BRL -> user.setBalanceBRL(user.getBalanceBRL().subtract(amount));
            case USD -> user.setBalanceUSD(user.getBalanceUSD().subtract(amount));
        }

        userRepository.save(user);
    }

    private void validateAmount(BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
    }
}