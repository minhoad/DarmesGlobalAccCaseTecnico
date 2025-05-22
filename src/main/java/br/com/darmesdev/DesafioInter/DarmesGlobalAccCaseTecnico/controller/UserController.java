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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Usuários", description = "APIs para gerenciamento de usuários")
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCreationStrategyManager strategyManager;

    @Operation(
            summary = "Criar um novo usuário pessoa física (PF)",
            description = "Cria um novo usuário pessoa física com CPF"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Email ou CPF já existem",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
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

    @Operation(
            summary = "Criar um novo usuário pessoa jurídica (PJ)",
            description = "Cria um novo usuário pessoa jurídica com CNPJ"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Email ou CNPJ já existem",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
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

    @Operation(
            summary = "Buscar usuário por ID",
            description = "Recupera os detalhes de um usuário pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userQueryService.findById(id));
    }

    @Operation(
            summary = "Listar todos os usuários",
            description = "Recupera uma lista paginada de todos os usuários"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuários recuperados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
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