package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.TransactionLimits;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@DiscriminatorValue("PF")
@NoArgsConstructor
public class UserPF extends User{
    @NotBlank
    @CPF
    @Column(unique = true)
    private String cpf;

    @Override
    public BigDecimal getDailyLimit() {
        return TransactionLimits.PF_DAILY_LIMIT;
    }
}
