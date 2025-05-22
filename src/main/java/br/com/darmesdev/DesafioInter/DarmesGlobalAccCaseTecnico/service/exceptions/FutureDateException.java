package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions;

import java.io.Serial;

public class FutureDateException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FutureDateException(String message) {
        super(message);
    }

    public FutureDateException(String message, Throwable cause) {
        super(message, cause);
    }
}