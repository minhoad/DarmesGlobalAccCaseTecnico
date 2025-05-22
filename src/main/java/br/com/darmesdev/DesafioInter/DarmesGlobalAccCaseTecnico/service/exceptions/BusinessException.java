package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions;

public abstract class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}