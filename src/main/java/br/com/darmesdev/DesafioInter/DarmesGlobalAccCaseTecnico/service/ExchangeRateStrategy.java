package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateStrategy {
    Optional<BigDecimal> getExchangeRate(LocalDate date);
}
