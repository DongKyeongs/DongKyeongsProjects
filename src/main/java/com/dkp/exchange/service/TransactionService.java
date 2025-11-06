package com.dkp.exchange.service;

import com.dkp.exchange.model.Transaction;
import com.dkp.exchange.model.User;
import com.dkp.exchange.repository.TransactionRepository;
import com.dkp.exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Transaction createDeposit(String asset, BigDecimal amount, String address) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setAsset(asset.toUpperCase());
        transaction.setAmount(amount);
        transaction.setAddress(address);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction createWithdrawal(String asset, BigDecimal amount, String address, String memo) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 잔액 확인
        BigDecimal currentBalance = getUserBalance(user, asset);
        if (currentBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setAsset(asset.toUpperCase());
        transaction.setAmount(amount);
        transaction.setAddress(address);
        transaction.setMemo(memo);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        // 잔액 차감 (출금 대기)
        updateUserBalance(user, asset, amount.negate());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction completeTransaction(Long transactionId, String txHash) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING &&
            transaction.getStatus() != Transaction.TransactionStatus.PROCESSING) {
            throw new RuntimeException("Transaction cannot be completed");
        }

        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setTxHash(txHash);
        transaction.setCompletedAt(LocalDateTime.now());

        // 입금인 경우 잔액 증가
        if (transaction.getType() == Transaction.TransactionType.DEPOSIT) {
            User user = transaction.getUser();
            updateUserBalance(user, transaction.getAsset(), transaction.getAmount());
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void cancelTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new RuntimeException("Transaction cannot be cancelled");
        }

        transaction.setStatus(Transaction.TransactionStatus.CANCELLED);

        // 출금 취소인 경우 잔액 복구
        if (transaction.getType() == Transaction.TransactionType.WITHDRAWAL) {
            updateUserBalance(user, transaction.getAsset(), transaction.getAmount());
        }

        transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getUserTransactions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public Transaction getTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return transaction;
    }

    private BigDecimal getUserBalance(User user, String asset) {
        return switch (asset.toUpperCase()) {
            case "USDT" -> user.getUsdtBalance();
            case "BTC" -> user.getBtcBalance();
            case "ETH" -> user.getEthBalance();
            default -> throw new RuntimeException("Unsupported asset: " + asset);
        };
    }

    private void updateUserBalance(User user, String asset, BigDecimal amount) {
        switch (asset.toUpperCase()) {
            case "USDT" -> user.setUsdtBalance(user.getUsdtBalance().add(amount));
            case "BTC" -> user.setBtcBalance(user.getBtcBalance().add(amount));
            case "ETH" -> user.setEthBalance(user.getEthBalance().add(amount));
            default -> throw new RuntimeException("Unsupported asset: " + asset);
        }
        userRepository.save(user);
    }
}
