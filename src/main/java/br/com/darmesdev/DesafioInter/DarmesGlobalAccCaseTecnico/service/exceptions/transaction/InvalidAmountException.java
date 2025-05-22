package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.BusinessException;

public class InvalidAmountException extends BusinessException {
    public InvalidAmountException() {
        super("Valor da transação deve ser positivo");
    }
}