package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.controller;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserPFRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserPJRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.UserResponse;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation.UserCreationStrategy;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.creation.UserCreationStrategyManager;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.BusinessException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.InvalidRequestException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.creation.DocumentAlreadyExistsException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.creation.EmailAlreadyExistsException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.exceptions.query.UserNotFoundException;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.query.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCreationStrategyManager strategyManager;

    @PostMapping("/pf")
    public ResponseEntity<UserResponse> createPFUser(@Valid @RequestBody UserPFRequest request) {
        try {
            User createdUser = strategyManager.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UserResponse.fromEntity(createdUser));
        } catch (EmailAlreadyExistsException | DocumentAlreadyExistsException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    @PostMapping("/pj")
    public ResponseEntity<UserResponse> createPJUser(@Valid @RequestBody UserPJRequest request) {
        try {
            User createdUser = strategyManager.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UserResponse.fromEntity(createdUser));
        } catch (EmailAlreadyExistsException | DocumentAlreadyExistsException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userQueryService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userQueryService.findAll(pageable));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<String> handleInvalidRequest(InvalidRequestException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler({EmailAlreadyExistsException.class, DocumentAlreadyExistsException.class})
    public ResponseEntity<String> handleConflictExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}