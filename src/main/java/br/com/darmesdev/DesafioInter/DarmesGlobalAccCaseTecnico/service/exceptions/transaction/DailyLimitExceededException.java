package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.BusinessException;

import java.math.BigDecimal;

public class DailyLimitExceededException extends BusinessException {
    public DailyLimitExceededException(BigDecimal limit) {
        super("Limite diário de " + limit + " excedido");
    }
}