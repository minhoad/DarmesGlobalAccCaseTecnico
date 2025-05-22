package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.transaction;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.BusinessException;

public class SelfTransferException extends BusinessException {
    public SelfTransferException() {
        super("Não é permitido enviar valores para si mesmo");
    }
}