package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String asset; // BTC, ETH, USDT 등

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(columnDefinition = "TEXT")
    private String address; // 입출금 주소

    @Column(columnDefinition = "TEXT")
    private String txHash; // 트랜잭션 해시

    @Column(columnDefinition = "TEXT")
    private String memo; // 메모

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    public enum TransactionType {
        DEPOSIT,    // 입금
        WITHDRAWAL  // 출금
    }

    public enum TransactionStatus {
        PENDING,    // 대기중
        PROCESSING, // 처리중
        COMPLETED,  // 완료
        FAILED,     // 실패
        CANCELLED   // 취소
    }
}
