package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.creation;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException  extends BusinessException {
    public EmailAlreadyExistsException(String email){
        super("E-mail já cadastrado: " + email);
    }
}
