package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.creation;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DocumentAlreadyExistsException extends BusinessException {
    public DocumentAlreadyExistsException(String document) {
        super("Documento já cadastrado: " + document);
    }
}