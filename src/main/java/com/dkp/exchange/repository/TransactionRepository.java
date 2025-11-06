package com.dkp.exchange.repository;

import com.dkp.exchange.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = :type ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndType(@Param("userId") Long userId, @Param("type") Transaction.TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.status = :status ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Transaction.TransactionStatus status);
}
