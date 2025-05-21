package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.controller;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.RemittanceRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.TransactionResponse;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.Transaction;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.RemittanceService;
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
public class RemittanceController {
    @Autowired
    private final RemittanceService remittanceService;

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
