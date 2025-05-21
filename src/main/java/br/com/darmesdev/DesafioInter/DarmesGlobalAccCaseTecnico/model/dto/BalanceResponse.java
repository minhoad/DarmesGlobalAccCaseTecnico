package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto;


import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.enums.Currency;

import java.math.BigDecimal;

public record BalanceResponse(
        Long userId,
        Currency currency,
        BigDecimal balance
) {}