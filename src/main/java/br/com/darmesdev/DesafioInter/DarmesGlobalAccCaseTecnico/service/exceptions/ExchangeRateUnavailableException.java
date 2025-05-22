package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExchangeRateUnavailableException extends RuntimeException {

    private final LocalDate date;
    private final String errorCode;

    public ExchangeRateUnavailableException(LocalDate date) {
        super("cotação indisponivel para o dia: " + date);
        this.date = date;
        this.errorCode = "EXCHANGE_RATE_UNAVAILABLE";
    }

    public ExchangeRateUnavailableException(String message, LocalDate date) {
        super(message);
        this.date = date;
        this.errorCode = "EXCHANGE_RATE_UNAVAILABLE";
    }

    public ExchangeRateUnavailableException(String message, LocalDate date, Throwable cause) {
        super(message, cause);
        this.date = date;
        this.errorCode = "EXCHANGE_RATE_UNAVAILABLE";
    }
    public ExchangeRateUnavailableException() {
        this(LocalDate.now());
    }

}