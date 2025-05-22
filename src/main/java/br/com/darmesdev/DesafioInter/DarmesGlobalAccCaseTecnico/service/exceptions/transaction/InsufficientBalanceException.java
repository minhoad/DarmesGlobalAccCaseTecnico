package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.BusinessException;

public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException() {
        super("Saldo insuficiente para realizar a transação");
    }
}
