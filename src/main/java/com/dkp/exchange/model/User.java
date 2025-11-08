package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private BigDecimal usdtBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal btcBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal ethBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean tradingEnabled = true;

    @Column(nullable = false)
    private boolean withdrawalEnabled = true;

    // VIP 시스템
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VipLevel vipLevel = VipLevel.NONE;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal totalTradingVolume30d = BigDecimal.ZERO; // 최근 30일 거래량

    @Column
    private LocalDateTime lastVipUpdate; // 마지막 VIP 등급 업데이트 시간

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal totalFeesPaid = BigDecimal.ZERO; // 총 납부한 수수료

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal totalFeesRefunded = BigDecimal.ZERO; // 총 환급받은 수수료

    @Version
    private Long version;
} 