package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public abstract sealed class UserRequest permits UserPFRequest, UserPJRequest {

    @NotBlank(message = "Nome completo é obrigatório")
    @JsonProperty("fullname")
    protected String fullName;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @JsonProperty("email")
    protected String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @JsonProperty("password")
    protected String password;

    // Getters
    public abstract String getDocument();
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}