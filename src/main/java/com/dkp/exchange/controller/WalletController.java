package com.dkp.exchange.controller;

import com.dkp.exchange.model.Transaction;
import com.dkp.exchange.model.Wallet;
import com.dkp.exchange.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<List<Wallet>> getWallets(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(walletService.getUserWallets(userId));
    }

    @GetMapping("/{asset}")
    public ResponseEntity<Wallet> getWallet(@PathVariable String asset, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(walletService.getWallet(userId, asset));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> requestDeposit(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        Transaction transaction = walletService.requestDeposit(
                userId,
                request.get("asset"),
                new BigDecimal(request.get("amount")),
                request.get("txHash"),
                request.get("fromAddress")
        );
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<Transaction> requestWithdrawal(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        Transaction transaction = walletService.requestWithdrawal(
                userId,
                request.get("asset"),
                new BigDecimal(request.get("amount")),
                request.get("toAddress"),
                request.get("memo")
        );
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdrawal/{id}/cancel")
    public ResponseEntity<Void> cancelWithdrawal(@PathVariable Long id) {
        walletService.cancelWithdrawal(id);
        return ResponseEntity.ok().build();
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        // 실제로는 UserDetailsService에서 가져와야 함
        return 1L; // 임시
    }
}
