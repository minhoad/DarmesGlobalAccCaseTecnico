package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amountBRL;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amountUSD;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal exchangeRate;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
