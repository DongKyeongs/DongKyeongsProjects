package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "advanced_orders")
public class AdvancedOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdvancedOrderType type;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.OrderSide side;

    // OCO Orders
    @Column(precision = 20, scale = 8)
    private BigDecimal stopPrice;      // 스탑 가격

    @Column(precision = 20, scale = 8)
    private BigDecimal limitPrice;     // 리밋 가격

    // Trailing Stop
    @Column(precision = 10, scale = 4)
    private BigDecimal trailingPercent; // 트레일링 퍼센트 (예: 5.0 = 5%)

    @Column(precision = 20, scale = 8)
    private BigDecimal trailingAmount;  // 트레일링 고정 금액

    @Column(precision = 20, scale = 8)
    private BigDecimal highestPrice;    // 최고가 (매도용)

    @Column(precision = 20, scale = 8)
    private BigDecimal lowestPrice;     // 최저가 (매수용)

    // Iceberg Order
    @Column(precision = 20, scale = 8)
    private BigDecimal totalQuantity;   // 전체 수량

    @Column(precision = 20, scale = 8)
    private BigDecimal visibleQuantity; // 노출 수량

    @Column(precision = 20, scale = 8)
    private BigDecimal filledQuantity = BigDecimal.ZERO;

    // 연관된 일반 주문들
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_order_id")
    private Order primaryOrder;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_order_id")
    private Order secondaryOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdvancedOrderStatus status = AdvancedOrderStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime triggeredAt;

    @Column
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum AdvancedOrderType {
        OCO,            // One-Cancels-Other
        TRAILING_STOP,  // Trailing Stop
        ICEBERG         // Iceberg Order (빙산 주문)
    }

    public enum AdvancedOrderStatus {
        PENDING,        // 대기중
        TRIGGERED,      // 트리거됨
        PARTIALLY_FILLED, // 부분 체결
        FILLED,         // 완전 체결
        CANCELLED,      // 취소됨
        EXPIRED         // 만료됨
    }
}
