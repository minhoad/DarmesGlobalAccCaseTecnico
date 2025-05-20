package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateStrategy {
    BigDecimal getExchangeRate(LocalDate date);
}
