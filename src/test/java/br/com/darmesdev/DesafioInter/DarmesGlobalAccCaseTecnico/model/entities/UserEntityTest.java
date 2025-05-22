package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserEntityTest {

    @Test
    @DisplayName("Deve criar e manipular um usuário corretamente")
    void createAndManipulateUser() {
        // Arrange - Criando uma implementação concreta de User para teste
        User user = new UserPF() {
            @Override
            public BigDecimal getDailyLimit() {
                return new BigDecimal("10000.00");
            }
        };

        // Act
        user.setId(1L);
        user.setEmail("teste@example.com");
        user.setPassword("senha123");
        user.setFullName("Usuário Teste"); // Usando o nome correto do campo
        user.setBalanceBRL(new BigDecimal("1000.00"));
        user.setBalanceUSD(new BigDecimal("200.00"));
        user.setVersion(0L);

        // Assert
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("teste@example.com", user.getEmail());
        assertEquals("senha123", user.getPassword());
        assertEquals("Usuário Teste", user.getFullName()); // Verificando com o nome correto do campo
        assertEquals(new BigDecimal("1000.00"), user.getBalanceBRL());
        assertEquals(new BigDecimal("200.00"), user.getBalanceUSD());
        assertEquals(0L, user.getVersion());
        assertEquals(new BigDecimal("10000.00"), user.getDailyLimit());
    }
}

