package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("PF")
public class UserPF extends User{
    @NotBlank
    @CPF
    @Column(unique = true)
    private String cpf;

    @Transient
    public BigDecimal getDailyLimit() {
        return new BigDecimal("10000");
    }
}
