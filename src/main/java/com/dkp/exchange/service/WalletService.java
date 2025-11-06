package com.dkp.exchange.service;

import com.dkp.exchange.exception.InsufficientBalanceException;
import com.dkp.exchange.exception.UserNotFoundException;
import com.dkp.exchange.exception.WithdrawalException;
import com.dkp.exchange.model.Transaction;
import com.dkp.exchange.model.User;
import com.dkp.exchange.model.Wallet;
import com.dkp.exchange.repository.TransactionRepository;
import com.dkp.exchange.repository.UserRepository;
import com.dkp.exchange.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Wallet> getUserWallets(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(Long userId, String asset) {
        return walletRepository.findByUserIdAndAsset(userId, asset)
                .orElseThrow(() -> new RuntimeException("Wallet not found for asset: " + asset));
    }

    @Transactional
    public Wallet createWallet(Long userId, String asset, String address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 중복 체크
        if (walletRepository.findByUserIdAndAsset(userId, asset).isPresent()) {
            throw new RuntimeException("Wallet already exists for asset: " + asset);
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setAsset(asset);
        wallet.setAddress(address);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setLockedBalance(BigDecimal.ZERO);

        return walletRepository.save(wallet);
    }

    @Transactional
    public Transaction requestDeposit(Long userId, String asset, BigDecimal amount, String txHash, String fromAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 중복 체크
        if (transactionRepository.findByTxHash(txHash).isPresent()) {
            throw new RuntimeException("Transaction already exists: " + txHash);
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTxHash(txHash);
        transaction.setAsset(asset);
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setAddress(fromAddress);
        transaction.setStatus(Transaction.TransactionStatus.CONFIRMING);
        transaction.setConfirmations(0);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction confirmDeposit(String txHash, Integer confirmations) {
        Transaction transaction = transactionRepository.findByTxHash(txHash)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setConfirmations(confirmations);

        // 필요한 확인 수에 도달하면 입금 처리
        if (confirmations >= transaction.getRequiredConfirmations()
            && transaction.getStatus() == Transaction.TransactionStatus.CONFIRMING) {

            Wallet wallet = walletRepository.findByUserIdAndAssetForUpdate(
                    transaction.getUser().getId(),
                    transaction.getAsset()
            ).orElseThrow(() -> new RuntimeException("Wallet not found"));

            wallet.credit(transaction.getAmount());
            walletRepository.save(wallet);

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());

            log.info("Deposit confirmed: {} {} for user {}",
                    transaction.getAmount(), transaction.getAsset(), transaction.getUser().getId());
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction requestWithdrawal(Long userId, String asset, BigDecimal amount, String toAddress, String memo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 지갑 조회 및 잠금
        Wallet wallet = walletRepository.findByUserIdAndAssetForUpdate(userId, asset)
                .orElseThrow(() -> new RuntimeException("Wallet not found for asset: " + asset));

        // 출금 수수료 계산
        BigDecimal fee = calculateWithdrawalFee(asset, amount);
        BigDecimal totalAmount = amount.add(fee);

        // 잔고 확인
        if (wallet.getAvailableBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientBalanceException(asset, totalAmount, wallet.getAvailableBalance());
        }

        // 잔고 차감
        wallet.debit(totalAmount);
        walletRepository.save(wallet);

        // 트랜잭션 생성
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTxHash(generateTxHash());
        transaction.setAsset(asset);
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setAddress(toAddress);
        transaction.setMemo(memo);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        transaction = transactionRepository.save(transaction);

        log.info("Withdrawal requested: {} {} for user {} to address {}",
                amount, asset, userId, toAddress);

        return transaction;
    }

    @Transactional
    public Transaction processWithdrawal(Long transactionId, String txHash) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getType() != Transaction.TransactionType.WITHDRAWAL) {
            throw new WithdrawalException("Not a withdrawal transaction");
        }

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new WithdrawalException("Transaction already processed");
        }

        transaction.setTxHash(txHash);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());

        log.info("Withdrawal processed: {} {} for user {}",
                transaction.getAmount(), transaction.getAsset(), transaction.getUser().getId());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void cancelWithdrawal(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getType() != Transaction.TransactionType.WITHDRAWAL) {
            throw new WithdrawalException("Not a withdrawal transaction");
        }

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new WithdrawalException("Cannot cancel processed transaction");
        }

        // 잔고 복구
        Wallet wallet = walletRepository.findByUserIdAndAssetForUpdate(
                transaction.getUser().getId(),
                transaction.getAsset()
        ).orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal totalAmount = transaction.getAmount().add(transaction.getFee());
        wallet.credit(totalAmount);
        walletRepository.save(wallet);

        transaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);

        log.info("Withdrawal cancelled: {} {} for user {}",
                transaction.getAmount(), transaction.getAsset(), transaction.getUser().getId());
    }

    private BigDecimal calculateWithdrawalFee(String asset, BigDecimal amount) {
        // 간단한 수수료 계산 (실제로는 설정에서 가져와야 함)
        switch (asset.toUpperCase()) {
            case "BTC":
                return new BigDecimal("0.0005");
            case "ETH":
                return new BigDecimal("0.005");
            case "USDT":
                return new BigDecimal("1.0");
            default:
                return BigDecimal.ZERO;
        }
    }

    private String generateTxHash() {
        return "TX-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
