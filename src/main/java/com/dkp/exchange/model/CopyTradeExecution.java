package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 카피 트레이드 실행 내역
 * 실제로 복사된 거래의 상세 기록
 */
@Data
@Entity
@Table(name = "copy_trade_executions", indexes = {
        @Index(name = "idx_execution_copy", columnList = "copy_trade_id"),
        @Index(name = "idx_execution_provider_order", columnList = "provider_order_id"),
        @Index(name = "idx_execution_follower_order", columnList = "follower_order_id"),
        @Index(name = "idx_execution_time", columnList = "executedAt")
})
public class CopyTradeExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copy_trade_id", nullable = false)
    private CopyTrade copyTrade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_order_id", nullable = false)
    private Order providerOrder; // 제공자의 원본 주문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_order_id")
    private Order followerOrder; // 팔로워의 복사된 주문

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status = ExecutionStatus.PENDING;

    // 거래 정보
    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.OrderSide side;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal providerAmount; // 제공자 거래 금액

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal followerAmount; // 팔로워 복사 금액

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal providerPrice; // 제공자 체결 가격

    @Column(precision = 20, scale = 8)
    private BigDecimal followerPrice; // 팔로워 체결 가격

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal copyRatio; // 적용된 복사 비율 (%)

    // 성과
    @Column(precision = 20, scale = 8)
    private BigDecimal profit; // 수익/손실

    @Column(precision = 10, scale = 2)
    private BigDecimal profitPercent; // 수익률 (%)

    @Column(precision = 20, scale = 8)
    private BigDecimal performanceFee; // 성과 수수료

    // 실행 정보
    @Column
    private String failureReason; // 실패 사유

    @Column(nullable = false)
    private LocalDateTime executedAt; // 실행 시간

    @Column
    private LocalDateTime completedAt; // 완료 시간

    @Column(nullable = false)
    private Long delayMs = 0L; // 지연 시간 (밀리초)

    public enum ExecutionStatus {
        PENDING,    // 대기 중
        EXECUTING,  // 실행 중
        COMPLETED,  // 완료
        FAILED,     // 실패
        CANCELLED   // 취소
    }

    /**
     * 슬리피지 계산 (%)
     */
    public BigDecimal calculateSlippage() {
        if (providerPrice == null || followerPrice == null || providerPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return followerPrice.subtract(providerPrice)
                .divide(providerPrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .abs();
    }

    /**
     * 수익/손실 계산
     */
    public void calculateProfit(BigDecimal currentPrice) {
        if (followerPrice == null || followerAmount == null) {
            return;
        }

        BigDecimal priceDiff = currentPrice.subtract(followerPrice);
        if (side == Order.OrderSide.SELL) {
            priceDiff = priceDiff.negate();
        }

        profit = priceDiff.multiply(followerAmount);

        if (followerPrice.compareTo(BigDecimal.ZERO) > 0) {
            profitPercent = profit.divide(followerPrice.multiply(followerAmount), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
    }
}
