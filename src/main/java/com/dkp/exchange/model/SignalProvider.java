package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 시그널 제공자 (카피 트레이딩 리더)
 * 자신의 거래를 공개하고 팔로워를 받음
 */
@Data
@Entity
@Table(name = "signal_providers", indexes = {
        @Index(name = "idx_provider_user", columnList = "user_id"),
        @Index(name = "idx_provider_status", columnList = "status"),
        @Index(name = "idx_provider_ranking", columnList = "ranking")
})
public class SignalProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String nickname; // 공개 닉네임

    @Column(length = 1000)
    private String description; // 자기소개

    @Column(length = 500)
    private String strategy; // 거래 전략 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderStatus status = ProviderStatus.ACTIVE;

    // 성과 지표
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalReturnPercent = BigDecimal.ZERO; // 총 수익률 (%)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyReturnPercent = BigDecimal.ZERO; // 월간 수익률 (%)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal winRate = BigDecimal.ZERO; // 승률 (%)

    @Column(nullable = false)
    private Integer totalTrades = 0; // 총 거래 수

    @Column(nullable = false)
    private Integer winningTrades = 0; // 이긴 거래 수

    @Column(nullable = false)
    private Integer losingTrades = 0; // 진 거래 수

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxDrawdown = BigDecimal.ZERO; // 최대 손실률 (%)

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal totalVolume = BigDecimal.ZERO; // 총 거래량 (USDT)

    // 팔로워 정보
    @Column(nullable = false)
    private Integer followerCount = 0; // 팔로워 수

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal totalCopiedVolume = BigDecimal.ZERO; // 복사된 총 거래량

    // 수수료 설정
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal performanceFeePercent = BigDecimal.ZERO; // 성과 수수료 (%)

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal minCopyAmount = new BigDecimal("100"); // 최소 복사 금액

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal maxCopyAmount = new BigDecimal("100000"); // 최대 복사 금액

    @Column(nullable = false)
    private Integer maxFollowers = 1000; // 최대 팔로워 수

    // 랭킹
    @Column(nullable = false)
    private Integer ranking = 9999; // 순위 (낮을수록 상위)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rankingScore = BigDecimal.ZERO; // 랭킹 점수

    // 활성화 정보
    @Column(nullable = false)
    private boolean acceptingFollowers = true; // 팔로워 받기

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime lastTradeAt; // 마지막 거래 시간

    public enum ProviderStatus {
        ACTIVE,      // 활성
        INACTIVE,    // 비활성
        SUSPENDED,   // 정지
        BANNED       // 차단
    }

    /**
     * 팔로워 추가 가능 여부
     */
    public boolean canAcceptFollower() {
        return status == ProviderStatus.ACTIVE
                && acceptingFollowers
                && followerCount < maxFollowers;
    }

    /**
     * 성과 수수료 계산
     */
    public BigDecimal calculatePerformanceFee(BigDecimal profit) {
        if (profit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return profit.multiply(performanceFeePercent).divide(new BigDecimal("100"), 8, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 승률 계산
     */
    public void updateWinRate() {
        if (totalTrades == 0) {
            winRate = BigDecimal.ZERO;
        } else {
            winRate = new BigDecimal(winningTrades)
                    .divide(new BigDecimal(totalTrades), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
    }
}
