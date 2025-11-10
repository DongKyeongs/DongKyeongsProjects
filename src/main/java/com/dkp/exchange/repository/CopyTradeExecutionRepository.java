package com.dkp.exchange.repository;

import com.dkp.exchange.model.CopyTradeExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CopyTradeExecutionRepository extends JpaRepository<CopyTradeExecution, Long> {
    List<CopyTradeExecution> findByCopyTradeId(Long copyTradeId);

    List<CopyTradeExecution> findByProviderOrderId(Long providerOrderId);

    @Query("SELECT e FROM CopyTradeExecution e WHERE e.copyTrade.follower.id = :followerId ORDER BY e.executedAt DESC")
    List<CopyTradeExecution> findByFollowerId(@Param("followerId") Long followerId);

    @Query("SELECT e FROM CopyTradeExecution e WHERE e.copyTrade.provider.id = :providerId ORDER BY e.executedAt DESC")
    List<CopyTradeExecution> findByProviderId(@Param("providerId") Long providerId);

    @Query("SELECT e FROM CopyTradeExecution e WHERE e.copyTrade.id = :copyTradeId AND e.executedAt >= :since")
    List<CopyTradeExecution> findByCopyTradeIdSince(@Param("copyTradeId") Long copyTradeId, @Param("since") LocalDateTime since);
}
