package com.dkp.exchange.controller;

import com.dkp.exchange.model.Transaction;
import com.dkp.exchange.service.TransactionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> createDeposit(@RequestBody DepositRequest request) {
        Transaction transaction = transactionService.createDeposit(
                request.getAsset(),
                request.getAmount(),
                request.getAddress()
        );
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<Transaction> createWithdrawal(@RequestBody WithdrawalRequest request) {
        Transaction transaction = transactionService.createWithdrawal(
                request.getAsset(),
                request.getAmount(),
                request.getAddress(),
                request.getMemo()
        );
        return ResponseEntity.ok(transaction);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getUserTransactions() {
        return ResponseEntity.ok(transactionService.getUserTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Transaction> completeTransaction(
            @PathVariable Long id,
            @RequestBody CompleteTransactionRequest request) {
        Transaction transaction = transactionService.completeTransaction(id, request.getTxHash());
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTransaction(@PathVariable Long id) {
        transactionService.cancelTransaction(id);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class DepositRequest {
        private String asset;
        private BigDecimal amount;
        private String address;
    }

    @Data
    public static class WithdrawalRequest {
        private String asset;
        private BigDecimal amount;
        private String address;
        private String memo;
    }

    @Data
    public static class CompleteTransactionRequest {
        private String txHash;
    }
}
