package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "staking_products")
public class StakingProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String asset; // BTC, ETH, USDT 등

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StakingType type;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal aprRate; // 연이율 (예: 5.0 = 5%)

    @Column(nullable = false)
    private Integer durationDays; // 기간 (일) - 0이면 Flexible

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal minAmount; // 최소 스테이킹 금액

    @Column(precision = 20, scale = 8)
    private BigDecimal maxAmount; // 최대 스테이킹 금액

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal totalStaked = BigDecimal.ZERO; // 총 스테이킹된 양

    @Column(precision = 20, scale = 8)
    private BigDecimal maxTotalStake; // 최대 총 스테이킹 한도

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum StakingType {
        FLEXIBLE,    // 언제든지 해제 가능
        LOCKED       // 고정 기간
    }

    public boolean hasCapacityFor(BigDecimal amount) {
        if (maxTotalStake == null) return true;
        return totalStaked.add(amount).compareTo(maxTotalStake) <= 0;
    }
}
