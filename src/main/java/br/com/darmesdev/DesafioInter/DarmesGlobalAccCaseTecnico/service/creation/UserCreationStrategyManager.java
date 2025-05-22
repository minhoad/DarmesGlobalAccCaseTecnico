package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserCreationStrategyManager {

    private final List<UserCreationStrategy<? extends UserRequest>> strategies;

    public User createUser(UserRequest request) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(request.getClass()))
                .findFirst()
                .map(strategy -> executeStrategy(strategy, request))
                .orElseThrow(() -> new IllegalArgumentException("Tipo de requisição não suportado"));
    }

    @SuppressWarnings("unchecked")
    private <T extends UserRequest> User executeStrategy(UserCreationStrategy<T> strategy, UserRequest request) {
        return strategy.create((T) request);
    }
}