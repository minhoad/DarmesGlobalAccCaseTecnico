package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CNPJ;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("PJ")
public class UserPJ extends User{
    @NotBlank
    @CNPJ
    @Column(unique = true)
    private String cnpj;

    @Transient
    public BigDecimal getDailyLimit() {
        return new BigDecimal("50000");
    }
}
