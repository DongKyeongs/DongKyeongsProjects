package com.dkp.exchange.repository;

import com.dkp.exchange.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Transaction> findByTxHash(String txHash);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = :type ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndType(@Param("userId") Long userId, @Param("type") Transaction.TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE t.status = :status AND t.type = 'DEPOSIT'")
    List<Transaction> findPendingDeposits(@Param("status") Transaction.TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.createdAt >= :startDate")
    List<Transaction> findByUserIdAndDateAfter(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
}
