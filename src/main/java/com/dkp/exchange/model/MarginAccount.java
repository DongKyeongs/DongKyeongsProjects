package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "margin_accounts")
public class MarginAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal totalBalance = BigDecimal.ZERO; // 총 잔고

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal availableBalance = BigDecimal.ZERO; // 사용 가능 잔고

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal borrowedAmount = BigDecimal.ZERO; // 빌린 금액

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal interest = BigDecimal.ZERO; // 누적 이자

    @Column(nullable = false)
    private Integer maxLeverage = 10; // 최대 레버리지 (기본 10x)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarginAccountStatus status = MarginAccountStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BigDecimal getEquity() {
        // 순자산 = 총잔고 - 빌린금액 - 이자
        return totalBalance.subtract(borrowedAmount).subtract(interest);
    }

    public BigDecimal getMarginLevel() {
        // 마진 레벨 = (순자산 / 빌린금액) * 100
        if (borrowedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("999999");
        }
        return getEquity().divide(borrowedAmount, 2, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public boolean isLiquidationRequired() {
        // 마진 레벨이 120% 이하면 강제 청산
        return getMarginLevel().compareTo(new BigDecimal("120")) < 0;
    }

    public enum MarginAccountStatus {
        ACTIVE,     // 활성
        SUSPENDED,  // 정지
        LIQUIDATING // 청산중
    }
}
