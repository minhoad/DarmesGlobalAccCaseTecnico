package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CNPJ;
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class UserPJRequest extends UserRequest {

    @NotBlank(message = "CNPJ é obrigatório")
    @CNPJ(message = "CNPJ inválido")
    @JsonProperty("cnpj")
    private String cnpj;

    public UserPJRequest(String fullName, String email, String password, String cnpj) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.cnpj = cnpj;
    }

    @Override
    public String getDocument() { return cnpj; }
}