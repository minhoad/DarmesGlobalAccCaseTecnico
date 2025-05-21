package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.controller;

import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.BalanceRequest;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.dto.BalanceResponse;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.enums.Currency;
import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.service.balance.BalanceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceManagementService balanceService;

    @GetMapping("/{userId}")
    public ResponseEntity<BalanceResponse> getBalance(
            @PathVariable Long userId,
            @RequestParam Currency currency
    ) {
        BigDecimal balance = balanceService.getBalance(userId, currency);
        return ResponseEntity.ok(new BalanceResponse(userId, currency, balance));
    }

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
