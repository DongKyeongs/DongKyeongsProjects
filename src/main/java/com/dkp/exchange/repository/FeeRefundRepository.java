package com.dkp.exchange.repository;

import com.dkp.exchange.model.FeeRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeeRefundRepository extends JpaRepository<FeeRefund, Long> {
    List<FeeRefund> findByUserId(Long userId);

    List<FeeRefund> findByStatus(FeeRefund.RefundStatus status);

    @Query("SELECT f FROM FeeRefund f WHERE f.user.id = :userId AND f.refundedAt >= :since")
    List<FeeRefund> findByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT SUM(f.refundAmount) FROM FeeRefund f WHERE f.user.id = :userId AND f.status = 'PROCESSED'")
    java.math.BigDecimal getTotalRefundedByUserId(@Param("userId") Long userId);
}
