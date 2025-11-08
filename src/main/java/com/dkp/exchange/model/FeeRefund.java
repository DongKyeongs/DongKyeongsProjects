package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 수수료 환급 내역
 * 월간 거래량 달성 시 수수료 환급
 */
@Data
@Entity
@Table(name = "fee_refunds", indexes = {
        @Index(name = "idx_user_refund", columnList = "user_id"),
        @Index(name = "idx_refund_date", columnList = "refundedAt")
})
public class FeeRefund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal originalFee; // 원래 납부한 수수료

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal refundAmount; // 환급 금액

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal refundRate; // 환급율 (0.1 = 10%)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundReason reason;

    @Column(nullable = false)
    private String asset = "USDT"; // 환급 자산 (기본 USDT)

    @Column(nullable = false)
    private LocalDateTime refundedAt;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status = RefundStatus.PENDING;

    @Column
    private LocalDateTime processedAt;

    public enum RefundReason {
        MONTHLY_VOLUME_ACHIEVEMENT,  // 월간 거래량 달성
        VIP_UPGRADE_BONUS,           // VIP 등급 업그레이드 보너스
        PROMOTIONAL_EVENT,           // 프로모션 이벤트
        ADMIN_ADJUSTMENT,            // 관리자 조정
        EXCHANGE_TOKEN_HOLDING       // 거래소 토큰 보유
    }

    public enum RefundStatus {
        PENDING,    // 대기중
        APPROVED,   // 승인됨
        PROCESSED,  // 처리완료 (지갑에 입금됨)
        REJECTED,   // 거부됨
        CANCELLED   // 취소됨
    }
}
