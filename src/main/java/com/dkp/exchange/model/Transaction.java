package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_tx_hash", columnList = "txHash"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String txHash; // 블록체인 트랜잭션 해시

    @Column(nullable = false)
    private String asset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(nullable = false)
    private String address; // 입금 시: 보낸 주소, 출금 시: 받는 주소

    @Column
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column
    private Integer confirmations = 0;

    @Column(nullable = false)
    private Integer requiredConfirmations = 6;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TransactionType {
        DEPOSIT,    // 입금
        WITHDRAWAL  // 출금
    }

    public enum TransactionStatus {
        PENDING,     // 대기중
        CONFIRMING,  // 확인중
        COMPLETED,   // 완료
        FAILED,      // 실패
        CANCELLED    // 취소
    }
}
