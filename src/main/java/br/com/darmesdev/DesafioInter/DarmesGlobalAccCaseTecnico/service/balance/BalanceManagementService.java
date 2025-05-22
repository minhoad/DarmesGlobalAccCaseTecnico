package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.balance;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.enums.Currency;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction.InsufficientBalanceException;

import java.math.BigDecimal;


public interface BalanceManagementService {
    BigDecimal getBalance(Long userId, Currency currency);
    void addBalance(Long userId, BigDecimal amount, Currency currency);
    void deductBalance(Long userId, BigDecimal amount, Currency currency)
            throws InsufficientBalanceException;
}