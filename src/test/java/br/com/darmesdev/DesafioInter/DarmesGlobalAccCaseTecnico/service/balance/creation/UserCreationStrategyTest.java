package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.balance.creation;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserPFRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserPJRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPF;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPJ;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.UserRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation.strategies.PFUserCreationStrategy;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation.strategies.PJUserCreationStrategy;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.creation.DocumentAlreadyExistsException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.creation.EmailAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserCreationStrategyTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PFUserCreationStrategy pfStrategy;

    @InjectMocks
    private PJUserCreationStrategy pjStrategy;

    @Test
    @DisplayName("Deve criar usuário PF com sucesso quando dados são válidos")
    void createPFUser_WithValidData_ShouldSucceed() {
        // Arrange
        UserPFRequest request = new UserPFRequest(
                "João Silva",
                "joao.silva@example.com",
                "senha123",
                "123.456.789-00"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByCpf(anyString())).thenReturn(false);
        when(userRepository.save(any(UserPF.class))).thenAnswer(invocation -> {
            UserPF user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        User result = pfStrategy.create(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UserPF.class);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getFullName()).isEqualTo("João Silva");
        assertThat(result.getEmail()).isEqualTo("joao.silva@example.com");
        assertThat(((UserPF) result).getCpf()).isEqualTo("123.456.789-00");
        assertThat(result.getDailyLimit()).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("Deve criar usuário PJ com sucesso quando dados são válidos")
    void createPJUser_WithValidData_ShouldSucceed() {
        // Arrange
        UserPJRequest request = new UserPJRequest(
                "Empresa XYZ",
                "contato@empresaxyz.com",
                "senha123",
                "12.345.678/0001-90"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByCnpj(anyString())).thenReturn(false);
        when(userRepository.save(any(UserPJ.class))).thenAnswer(invocation -> {
            UserPJ user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        User result = pjStrategy.create(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UserPJ.class);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Empresa XYZ");
        assertThat(result.getEmail()).isEqualTo("contato@empresaxyz.com");
        assertThat(((UserPJ) result).getCnpj()).isEqualTo("12.345.678/0001-90");
        assertThat(result.getDailyLimit()).isEqualByComparingTo(new BigDecimal("50000.00"));
    }

    @Test
    @DisplayName("Deve lançar EmailAlreadyExistsException quando email já existe")
    void createUser_WithExistingEmail_ShouldThrowEmailAlreadyExistsException() {
        // Arrange
        UserPFRequest request = new UserPFRequest(
                "João Silva",
                "email.existente@example.com",
                "senha123",
                "123.456.789-00"
        );

        when(userRepository.existsByEmail("email.existente@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> pfStrategy.create(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("email.existente@example.com");
    }

    @Test
    @DisplayName("Deve lançar DocumentAlreadyExistsException quando CPF já existe")
    void createPFUser_WithExistingCPF_ShouldThrowDocumentAlreadyExistsException() {
        // Arrange
        UserPFRequest request = new UserPFRequest(
                "João Silva",
                "joao.silva@example.com",
                "senha123",
                "123.456.789-00"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByCpf("123.456.789-00")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> pfStrategy.create(request))
                .isInstanceOf(DocumentAlreadyExistsException.class)
                .hasMessageContaining("123.456.789-00");
    }

    @Test
    @DisplayName("Deve lançar DocumentAlreadyExistsException quando CNPJ já existe")
    void createPJUser_WithExistingCNPJ_ShouldThrowDocumentAlreadyExistsException() {
        // Arrange
        UserPJRequest request = new UserPJRequest(
                "Empresa XYZ",
                "contato@empresaxyz.com",
                "senha123",
                "12.345.678/0001-90"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByCnpj("12.345.678/0001-90")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> pjStrategy.create(request))
                .isInstanceOf(DocumentAlreadyExistsException.class)
                .hasMessageContaining("12.345.678/0001-90");
    }
}
