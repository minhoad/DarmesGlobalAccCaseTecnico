package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.controller;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.RemittanceRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.TransactionResponse;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.Transaction;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.RemittanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/remittances")
@RequiredArgsConstructor
@Tag(name = "Remessas", description = "API de operações de remessas")
public class RemittanceController {
    @Autowired
    private final RemittanceService remittanceService;
    @Operation(
            summary = "Criar uma nova remessa",
            description = "Transfere fundos de um usuário para outro, convertendo de BRL para USD"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Remessa realizada com sucesso",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de requisição inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente ou limite diário excedido",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> createRemittance(@RequestBody RemittanceRequest request) {
        Transaction transaction = remittanceService.executeRemittance(
                request.senderId(),
                request.receiverId(),
                request.amountBRL()
        );
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }
}
