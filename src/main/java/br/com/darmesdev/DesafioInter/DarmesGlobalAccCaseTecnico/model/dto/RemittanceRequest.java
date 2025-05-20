package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RemittanceRequest(
        @NotNull(message = "Sender ID é obrigatório")
        Long senderId,

        @NotNull(message = "Receiver ID é obrigatório")
        Long receiverId,

        @Positive(message = "O valor deve ser positivo")
        @DecimalMin(value = "0.01", message = "O valor mínimo é R$ 0,01")
        BigDecimal amountBRL
) {}