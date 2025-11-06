package com.dkp.exchange.repository;

import com.dkp.exchange.model.StakingPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StakingPositionRepository extends JpaRepository<StakingPosition, Long> {
    List<StakingPosition> findByUserIdAndStatus(Long userId, StakingPosition.StakingPositionStatus status);

    @Query("SELECT s FROM StakingPosition s WHERE s.status = 'ACTIVE'")
    List<StakingPosition> findAllActive();

    @Query("SELECT SUM(s.stakedAmount) FROM StakingPosition s WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    java.math.BigDecimal getTotalStakedByUser(@Param("userId") Long userId);
}
