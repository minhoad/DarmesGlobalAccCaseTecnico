package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.balance.creation;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserPFRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserPJRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPF;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPJ;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.UserRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation.strategies.PFUserCreationStrategy;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation.strategies.PJUserCreationStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDailyLimitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PFUserCreationStrategy pfStrategy;

    @InjectMocks
    private PJUserCreationStrategy pjStrategy;

    @Test
    @DisplayName("Deve criar usuário PF com limite diário de 10 mil reais")
    void createPFUser_ShouldHaveDailyLimitOf10000() {
        // Arrange
        UserPFRequest request = new UserPFRequest(
                "João Silva",
                "joao.silva@example.com",
                "senha123",
                "123.456.789-00"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByCpf(anyString())).thenReturn(false);
        when(userRepository.save(any(UserPF.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = pfStrategy.create(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UserPF.class);
        assertThat(result.getDailyLimit()).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("Deve criar usuário PJ com limite diário de 50 mil reais")
    void createPJUser_ShouldHaveDailyLimitOf50000() {
        // Arrange
        UserPJRequest request = new UserPJRequest(
                "Empresa XYZ",
                "contato@empresaxyz.com",
                "senha123",
                "12.345.678/0001-90"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByCnpj(anyString())).thenReturn(false);
        when(userRepository.save(any(UserPJ.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = pjStrategy.create(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UserPJ.class);
        assertThat(result.getDailyLimit()).isEqualByComparingTo(new BigDecimal("50000.00"));
    }
}
