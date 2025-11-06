package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "staking_positions")
public class StakingPosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private StakingProduct product;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal stakedAmount;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal earnedRewards = BigDecimal.ZERO; // 누적 보상

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal pendingRewards = BigDecimal.ZERO; // 대기중인 보상

    @Column(nullable = false)
    private LocalDateTime stakedAt;

    @Column
    private LocalDateTime unstakedAt;

    @Column
    private LocalDateTime maturityDate; // 만기일 (LOCKED인 경우)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StakingPositionStatus status = StakingPositionStatus.ACTIVE;

    @Column
    private LocalDateTime lastRewardCalculatedAt;

    @PrePersist
    protected void onCreate() {
        stakedAt = LocalDateTime.now();
        lastRewardCalculatedAt = LocalDateTime.now();

        // LOCKED 타입이면 만기일 설정
        if (product.getType() == StakingProduct.StakingType.LOCKED) {
            maturityDate = stakedAt.plusDays(product.getDurationDays());
        }
    }

    public boolean canUnstake() {
        if (status != StakingPositionStatus.ACTIVE) {
            return false;
        }

        // FLEXIBLE이면 언제든지 가능
        if (product.getType() == StakingProduct.StakingType.FLEXIBLE) {
            return true;
        }

        // LOCKED면 만기일 이후에만 가능
        return LocalDateTime.now().isAfter(maturityDate);
    }

    public enum StakingPositionStatus {
        ACTIVE,      // 활성
        UNSTAKED,    // 해제됨
        COMPLETED    // 완료됨
    }
}
