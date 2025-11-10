package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 카피 트레이드 설정
 * 팔로워가 시그널 제공자를 복사하는 설정
 */
@Data
@Entity
@Table(name = "copy_trades", indexes = {
        @Index(name = "idx_copy_follower", columnList = "follower_id"),
        @Index(name = "idx_copy_provider", columnList = "provider_id"),
        @Index(name = "idx_copy_status", columnList = "status")
})
public class CopyTrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower; // 팔로워 (복사하는 사람)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private SignalProvider provider; // 시그널 제공자 (복사당하는 사람)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CopyStatus status = CopyStatus.ACTIVE;

    // 복사 설정
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CopyMode mode = CopyMode.PROPORTIONAL; // 복사 모드

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal copyAmount; // 복사 금액 (고정 금액 모드)

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal copyRatio = new BigDecimal("100"); // 복사 비율 (%) (비례 모드)

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal maxPerTrade = new BigDecimal("10000"); // 거래당 최대 금액

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal minPerTrade = new BigDecimal("10"); // 거래당 최소 금액

    // 리스크 관리
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal stopLossPercent = BigDecimal.ZERO; // 손절 (0 = 비활성화)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal takeProfitPercent = BigDecimal.ZERO; // 익절 (0 = 비활성화)

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal maxDailyLoss = BigDecimal.ZERO; // 일일 최대 손실 (0 = 비활성화)

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal maxTotalLoss = BigDecimal.ZERO; // 총 최대 손실 (0 = 비활성화)

    // 필터
    private String symbolFilter; // 심볼 필터 (콤마 구분: "BTC/USDT,ETH/USDT")

    @Column(nullable = false)
    private boolean copyMarketOrders = true; // 시장가 주문 복사

    @Column(nullable = false)
    private boolean copyLimitOrders = true; // 지정가 주문 복사

    @Column(nullable = false)
    private boolean copyStopOrders = true; // 스탑 주문 복사

    // 성과 추적
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal totalProfit = BigDecimal.ZERO; // 총 수익

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal totalLoss = BigDecimal.ZERO; // 총 손실

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal netProfit = BigDecimal.ZERO; // 순 수익

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal returnPercent = BigDecimal.ZERO; // 수익률 (%)

    @Column(nullable = false)
    private Integer copiedTrades = 0; // 복사된 거래 수

    @Column(nullable = false)
    private Integer successfulTrades = 0; // 성공한 거래 수

    @Column(nullable = false)
    private Integer failedTrades = 0; // 실패한 거래 수

    // 시간 정보
    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column
    private LocalDateTime stoppedAt;

    @Column
    private LocalDateTime lastCopiedAt; // 마지막 복사 시간

    public enum CopyStatus {
        ACTIVE,    // 활성 (복사 중)
        PAUSED,    // 일시정지
        STOPPED,   // 중단
        COMPLETED  // 완료
    }

    public enum CopyMode {
        FIXED,        // 고정 금액 (항상 같은 금액으로 복사)
        PROPORTIONAL  // 비례 (제공자 거래 금액의 일정 비율로 복사)
    }

    /**
     * 심볼 필터 체크
     */
    public boolean isSymbolAllowed(String symbol) {
        if (symbolFilter == null || symbolFilter.isEmpty()) {
            return true; // 필터 없으면 모든 심볼 허용
        }
        String[] allowedSymbols = symbolFilter.split(",");
        for (String allowed : allowedSymbols) {
            if (allowed.trim().equalsIgnoreCase(symbol)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 주문 타입 필터 체크
     */
    public boolean isOrderTypeAllowed(Order.OrderType orderType) {
        return switch (orderType) {
            case MARKET -> copyMarketOrders;
            case LIMIT -> copyLimitOrders;
            case STOP_LOSS, STOP_LIMIT -> copyStopOrders;
        };
    }

    /**
     * 복사 금액 계산
     */
    public BigDecimal calculateCopyAmount(BigDecimal providerAmount) {
        BigDecimal amount;

        if (mode == CopyMode.FIXED) {
            amount = copyAmount;
        } else {
            // 비례 모드: 제공자 금액 * 복사 비율
            amount = providerAmount.multiply(copyRatio).divide(new BigDecimal("100"), 8, java.math.RoundingMode.HALF_UP);
        }

        // 최소/최대 금액 제한 적용
        if (amount.compareTo(minPerTrade) < 0) {
            amount = minPerTrade;
        }
        if (amount.compareTo(maxPerTrade) > 0) {
            amount = maxPerTrade;
        }

        return amount;
    }

    /**
     * 손실 제한 체크
     */
    public boolean isWithinLossLimit(BigDecimal additionalLoss) {
        // 일일 최대 손실 체크
        if (maxDailyLoss.compareTo(BigDecimal.ZERO) > 0) {
            // TODO: 일일 손실 계산 로직 추가
        }

        // 총 최대 손실 체크
        if (maxTotalLoss.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal projectedLoss = totalLoss.add(additionalLoss);
            if (projectedLoss.compareTo(maxTotalLoss) > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 성과 업데이트
     */
    public void updatePerformance(BigDecimal profit) {
        if (profit.compareTo(BigDecimal.ZERO) > 0) {
            totalProfit = totalProfit.add(profit);
            successfulTrades++;
        } else {
            totalLoss = totalLoss.add(profit.abs());
            failedTrades++;
        }

        netProfit = totalProfit.subtract(totalLoss);
        copiedTrades++;

        // 수익률 계산
        if (copyAmount.compareTo(BigDecimal.ZERO) > 0) {
            returnPercent = netProfit.divide(copyAmount, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        }
    }
}
