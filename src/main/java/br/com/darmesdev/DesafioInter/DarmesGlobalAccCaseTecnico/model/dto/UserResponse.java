package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPF;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPJ;

import java.math.BigDecimal;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String document,
        BigDecimal balanceBRL,
        BigDecimal balanceUSD,
        String userType
) {
    public static UserResponse fromEntity(User user) {
        String document = "";
        String userType = "";

        if (user instanceof UserPF) {
            document = ((UserPF) user).getCpf();
            userType = "PF";
        } else if (user instanceof UserPJ) {
            document = ((UserPJ) user).getCnpj();
            userType = "PJ";
        }

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                document,
                user.getBalanceBRL(),
                user.getBalanceUSD(),
                userType
        );
    }
}