package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation.strategies;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserPFRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserPJRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPJ;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.UserRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation.UserCreationStrategy;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.creation.DocumentAlreadyExistsException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.creation.EmailAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PJUserCreationStrategy implements UserCreationStrategy<UserPJRequest> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserPJ create(UserPJRequest request) {
        validateUniqueConstraints(request);

        UserPJ user = new UserPJ();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setCnpj(request.getDocument());
        return userRepository.save(user);
    }

    private void validateUniqueConstraints(UserPJRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        if (userRepository.existsByCnpj(request.getDocument())) {
            throw new DocumentAlreadyExistsException(request.getDocument());
        }
    }

    @Override
    public boolean supports(Class<? extends UserRequest> requestType) {
        return UserPJRequest.class.isAssignableFrom(requestType);
    }
}
