package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.TransactionLimits;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.br.CNPJ;

import java.math.BigDecimal;
@Data
@Entity
@DiscriminatorValue("PJ")
public class UserPJ extends User{
    @NotBlank
    @CNPJ
    @Column(unique = true)
    private String cnpj;

    @Override
    public BigDecimal getDailyLimit() {
        return TransactionLimits.PJ_DAILY_LIMIT;
    }
}
