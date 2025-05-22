package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.controller;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.BalanceRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.BalanceResponse;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.enums.Currency;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.balance.BalanceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
@Tag(name = "Saldo", description = "APIs para gerenciamento de saldo")
public class BalanceController {

    private final BalanceManagementService balanceService;


    @Operation(
            summary = "Consultar saldo do usuário",
            description = "Recupera o saldo de um usuário específico na moeda solicitada"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação bem-sucedida",
                    content = @Content(schema = @Schema(implementation = BalanceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    @GetMapping("/{userId}")
    public ResponseEntity<BalanceResponse> getBalance(
            @PathVariable Long userId,
            @RequestParam Currency currency
    ) {
        BigDecimal balance = balanceService.getBalance(userId, currency);
        return ResponseEntity.ok(new BalanceResponse(userId, currency, balance));
    }

    @Operation(
            summary = "Depositar fundos",
            description = "Adiciona fundos à conta de um usuário na moeda especificada"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = BalanceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Valor inválido",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    @PostMapping("/deposit/{userId}")
    public ResponseEntity<BalanceResponse> deposit(
            @PathVariable Long userId,
            @RequestParam Currency currency,
            @Valid @RequestBody BalanceRequest request
    ) {
        balanceService.addBalance(userId, request.amount(), currency);
        return ResponseEntity.ok(new BalanceResponse(userId, currency,
                balanceService.getBalance(userId, currency)));
    }

    @Operation(
            summary = "Sacar fundos",
            description = "Retira fundos da conta de um usuário na moeda especificada"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saque realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = BalanceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Valor inválido",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    @PostMapping("/withdraw/{userId}")
    public ResponseEntity<BalanceResponse> withdraw(
            @PathVariable Long userId,
            @RequestParam Currency currency,
            @Valid @RequestBody BalanceRequest request
    ) {
        balanceService.deductBalance(userId, request.amount(), currency);
        return ResponseEntity.ok(new BalanceResponse(userId, currency,
                balanceService.getBalance(userId, currency)));
    }
}
