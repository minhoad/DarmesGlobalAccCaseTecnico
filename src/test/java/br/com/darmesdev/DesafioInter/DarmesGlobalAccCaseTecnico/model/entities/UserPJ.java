package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("PJ")
public class UserPJ extends User {

    @NotBlank
    public String cnpj;

    private BigDecimal dailyLimit = new BigDecimal("50000.00");

    @Override
    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }
}
