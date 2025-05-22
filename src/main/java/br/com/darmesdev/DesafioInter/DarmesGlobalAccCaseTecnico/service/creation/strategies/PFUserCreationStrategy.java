package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation.strategies;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserPFRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.UserPF;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos.UserRepository;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation.UserCreationStrategy;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.creation.DocumentAlreadyExistsException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.creation.EmailAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PFUserCreationStrategy implements UserCreationStrategy<UserPFRequest> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserPF create(UserPFRequest request) {
        validateUniqueConstraints(request);

        UserPF user = new UserPF();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setCpf(request.getDocument());
        return userRepository.save(user);
    }

    private void validateUniqueConstraints(UserPFRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        if (userRepository.existsByCpf(request.getDocument())) {
            throw new DocumentAlreadyExistsException(request.getDocument());
        }
    }

    @Override
    public boolean supports(Class<? extends UserRequest> requestType) {
        return UserPFRequest.class.isAssignableFrom(requestType);
    }
}
