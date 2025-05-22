package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.query;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserResponse;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQueryService {
    User findEntityById(Long id); // Novo método para entidade
    UserResponse findById(Long id); // Mantém para DTO
    Page<UserResponse> findAll(Pageable pageable);
}
