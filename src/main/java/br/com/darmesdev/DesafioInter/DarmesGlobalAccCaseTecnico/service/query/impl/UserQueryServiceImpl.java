package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.query.impl;


import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserResponse;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.UserRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.query.UserNotFoundException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {


    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "users", key = "#id")
    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Cacheable(value = "userResponses", key = "#id")
    public UserResponse findById(Long id) {
        User user = findEntityById(id);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Cacheable(value = "userPages", key = "'page_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort")
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromEntity);
    }
}
