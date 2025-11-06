package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "p2p_trades")
public class P2PTrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_id", nullable = false)
    private P2PAdvertisement advertisement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount; // 거래량

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal price; // 단가

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal totalFiatAmount; // 총 법정화폐 금액

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal escrowAmount; // 에스크로된 암호화폐 양

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private P2PTradeStatus status = P2PTradeStatus.PENDING_PAYMENT;

    @Column
    private LocalDateTime paymentDeadline; // 결제 마감 시간

    @Column
    private LocalDateTime paidAt; // 결제 완료 시간

    @Column
    private LocalDateTime releasedAt; // 에스크로 해제 시간

    @Column(columnDefinition = "TEXT")
    private String buyerNotes; // 구매자 메모

    @Column(columnDefinition = "TEXT")
    private String disputeReason; // 분쟁 사유

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // 결제 마감 시간 설정
        paymentDeadline = createdAt.plusMinutes(advertisement.getTimeLimit());
    }

    public enum P2PTradeStatus {
        PENDING_PAYMENT,    // 결제 대기중
        PAID,               // 결제 완료, 판매자 확인 대기
        RELEASING,          // 에스크로 해제 중
        COMPLETED,          // 완료
        CANCELLED,          // 취소
        DISPUTED,           // 분쟁 중
        DISPUTE_RESOLVED    // 분쟁 해결됨
    }
}
