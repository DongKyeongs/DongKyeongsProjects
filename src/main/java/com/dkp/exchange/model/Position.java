package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "positions")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "margin_account_id", nullable = false)
    private MarginAccount marginAccount;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PositionSide side; // LONG or SHORT

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal entryPrice; // 진입 가격

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal liquidationPrice; // 청산 가격

    @Column(nullable = false)
    private Integer leverage; // 레버리지

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal margin; // 증거금

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal unrealizedPnL = BigDecimal.ZERO; // 미실현 손익

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal realizedPnL = BigDecimal.ZERO; // 실현 손익

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PositionStatus status = PositionStatus.OPEN;

    @Column(nullable = false)
    private LocalDateTime openedAt;

    @Column
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        openedAt = LocalDateTime.now();
    }

    public BigDecimal calculateUnrealizedPnL(BigDecimal currentPrice) {
        BigDecimal priceDiff;
        if (side == PositionSide.LONG) {
            priceDiff = currentPrice.subtract(entryPrice);
        } else {
            priceDiff = entryPrice.subtract(currentPrice);
        }
        return priceDiff.multiply(quantity);
    }

    public BigDecimal getPositionValue(BigDecimal currentPrice) {
        return currentPrice.multiply(quantity);
    }

    public enum PositionSide {
        LONG,   // 매수 포지션
        SHORT   // 매도 포지션
    }

    public enum PositionStatus {
        OPEN,        // 열림
        CLOSED,      // 닫힘
        LIQUIDATED   // 강제청산됨
    }
}
