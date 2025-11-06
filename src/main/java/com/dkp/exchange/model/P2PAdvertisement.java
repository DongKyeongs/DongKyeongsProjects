package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "p2p_advertisements")
public class P2PAdvertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private User merchant; // 광고주

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeType type; // BUY or SELL

    @Column(nullable = false)
    private String asset; // BTC, ETH 등

    @Column(nullable = false)
    private String fiatCurrency; // KRW, USD 등

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal price; // 단가

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal minAmount; // 최소 거래량

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal maxAmount; // 최대 거래량

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal availableAmount; // 사용 가능한 양

    @Column(nullable = false)
    private Integer timeLimit = 30; // 결제 시간 제한 (분)

    @Column(columnDefinition = "TEXT")
    private String terms; // 거래 조건

    @Column
    private String paymentMethod; // 결제 방법

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdvertisementStatus status = AdvertisementStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TradeType {
        BUY,  // 광고주가 매수
        SELL  // 광고주가 매도
    }

    public enum AdvertisementStatus {
        ACTIVE,     // 활성
        PAUSED,     // 일시중지
        COMPLETED,  // 완료
        CANCELLED   // 취소
    }
}
