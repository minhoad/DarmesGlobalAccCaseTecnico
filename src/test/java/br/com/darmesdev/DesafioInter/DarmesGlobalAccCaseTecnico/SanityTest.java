package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;

public class SanityTest {

    @Test
    @DisplayName("Teste de sanidade para verificar se o JUnit está funcionando")
    void sanityTest() {
        assertTrue(true, "Este teste deve sempre passar");
    }
}