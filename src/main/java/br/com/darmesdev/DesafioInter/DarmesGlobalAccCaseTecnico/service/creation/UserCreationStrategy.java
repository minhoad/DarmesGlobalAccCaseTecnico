package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;

public interface UserCreationStrategy<T extends UserRequest> {
    User create(T request);
    boolean supports(Class<? extends UserRequest> requestType); // Para decidir se essa strategy é a certa
}