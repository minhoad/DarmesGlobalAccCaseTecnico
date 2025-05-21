package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Campo obrigatório para o ID


    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private User receiver;

    @Column(name = "amount_brl")
    private BigDecimal amountBRL;

    @Column(name = "amount_usd")
    private BigDecimal amountUSD;

    private BigDecimal exchangeRate;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
