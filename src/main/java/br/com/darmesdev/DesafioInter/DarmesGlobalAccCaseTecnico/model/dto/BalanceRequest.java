package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BalanceRequest(
        @NotNull
        @DecimalMin(value = "0.01", message = "Valor deve ser positivo")
        BigDecimal amount
) {}
