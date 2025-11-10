package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 시그널 제공자 일일 성과
 * 매일 자정에 계산되는 성과 스냅샷
 */
@Data
@Entity
@Table(name = "signal_provider_performance", indexes = {
        @Index(name = "idx_performance_provider", columnList = "provider_id"),
        @Index(name = "idx_performance_date", columnList = "date")
})
public class SignalProviderPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private SignalProvider provider;

    @Column(nullable = false)
    private LocalDate date;

    // 일일 성과
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal dailyProfit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyReturnPercent = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer dailyTrades = 0;

    @Column(nullable = false)
    private Integer dailyWinningTrades = 0;

    @Column(nullable = false)
    private Integer dailyLosingTrades = 0;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal dailyVolume = BigDecimal.ZERO;

    // 누적 성과
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal cumulativeProfit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cumulativeReturnPercent = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer cumulativeTrades = 0;

    @Column(nullable = false)
    private Integer cumulativeWinningTrades = 0;

    @Column(nullable = false)
    private Integer cumulativeLosingTrades = 0;

    // 팔로워 정보
    @Column(nullable = false)
    private Integer followerCount = 0;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal totalCopiedVolume = BigDecimal.ZERO;

    // 최고/최저
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal peakBalance = BigDecimal.ZERO; // 최고 잔고

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal drawdown = BigDecimal.ZERO; // 현재 손실률

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxDrawdown = BigDecimal.ZERO; // 최대 손실률

    /**
     * 승률 계산
     */
    public BigDecimal getWinRate() {
        if (cumulativeTrades == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(cumulativeWinningTrades)
                .divide(new BigDecimal(cumulativeTrades), 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 평균 거래당 수익
     */
    public BigDecimal getAverageProfitPerTrade() {
        if (cumulativeTrades == 0) {
            return BigDecimal.ZERO;
        }
        return cumulativeProfit.divide(new BigDecimal(cumulativeTrades), 8, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 샤프 비율 (간단 버전 - 실제로는 더 복잡한 계산 필요)
     */
    public BigDecimal getSharpeRatio() {
        // TODO: 적절한 샤프 비율 계산
        return BigDecimal.ZERO;
    }
}
