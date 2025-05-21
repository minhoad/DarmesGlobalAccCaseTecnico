package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        BigDecimal brlAmount,
        BigDecimal usdAmount,
        BigDecimal exchangeRate,
        LocalDateTime timestamp,
        String senderName,
        String receiverEmail
) {
    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmountBRL(),
                transaction.getAmountUSD(),
                transaction.getExchangeRate(),
                transaction.getTimestamp(),
                transaction.getSender().getFullName(),
                transaction.getReceiver().getEmail()
        );
    }
}
