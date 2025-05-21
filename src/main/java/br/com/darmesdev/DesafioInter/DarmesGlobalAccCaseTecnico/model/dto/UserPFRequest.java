package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class UserPFRequest extends UserRequest {

    @NotBlank(message = "CPF é obrigatório")
    @CPF(message = "CPF inválido")
    @JsonProperty("cpf")
    private String cpf;

    public UserPFRequest(String fullName, String email, String password, String cpf) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.cpf = cpf;
    }

    @Override
    public String getDocument() { return cpf; }
}