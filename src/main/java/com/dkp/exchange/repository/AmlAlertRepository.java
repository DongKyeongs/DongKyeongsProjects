package com.dkp.exchange.repository;

import com.dkp.exchange.model.AmlAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AmlAlertRepository extends JpaRepository<AmlAlert, Long> {
    List<AmlAlert> findByUserIdAndStatus(Long userId, AmlAlert.AlertStatus status);

    @Query("SELECT a FROM AmlAlert a WHERE a.status = :status AND a.riskLevel = :riskLevel")
    List<AmlAlert> findByStatusAndRiskLevel(@Param("status") AmlAlert.AlertStatus status,
                                             @Param("riskLevel") AmlAlert.RiskLevel riskLevel);

    @Query("SELECT a FROM AmlAlert a WHERE a.user.id = :userId AND a.createdAt >= :since")
    List<AmlAlert> findByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM AmlAlert a WHERE a.user.id = :userId AND a.status = 'OPEN'")
    long countOpenAlertsByUserId(@Param("userId") Long userId);
}
