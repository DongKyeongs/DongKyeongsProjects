package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "exchange_settings")
public class ExchangeSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String theme = "dark"; // light, dark

    @Column(nullable = false)
    private String language = "ko"; // ko, en

    @Column(nullable = false)
    private boolean priceAlertsEnabled = true;

    @Column(nullable = false)
    private boolean orderNotificationsEnabled = true;

    @Column(nullable = false)
    private boolean systemNotificationsEnabled = true;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal makerFee = new BigDecimal("0.001"); // 0.1% 메이커 수수료

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal takerFee = new BigDecimal("0.002"); // 0.2% 테이커 수수료

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal minTradeAmount = new BigDecimal("10"); // 최소 거래 금액

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal maxTradeAmount = new BigDecimal("1000000"); // 최대 거래 금액
} 