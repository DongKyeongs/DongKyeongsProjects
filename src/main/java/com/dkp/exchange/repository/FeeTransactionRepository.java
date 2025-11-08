package com.dkp.exchange.repository;

import com.dkp.exchange.model.FeeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeeTransactionRepository extends JpaRepository<FeeTransaction, Long> {
    List<FeeTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT f FROM FeeTransaction f WHERE f.user.id = :userId")
    List<FeeTransaction> findByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(f.feeAmount) FROM FeeTransaction f WHERE f.user.id = :userId AND f.asset = :asset AND f.createdAt >= :startDate")
    BigDecimal sumFeesByUserAndAssetSince(@Param("userId") Long userId, @Param("asset") String asset, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(f.feeAmount) FROM FeeTransaction f WHERE f.asset = :asset AND f.createdAt >= :startDate")
    BigDecimal sumFeesByAssetSince(@Param("asset") String asset, @Param("startDate") LocalDateTime startDate);
}
