package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aml_alerts")
public class AmlAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(precision = 20, scale = 8)
    private BigDecimal amount;

    @Column
    private String relatedTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status = AlertStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String investigationNotes;

    @Column
    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum AlertType {
        LARGE_TRANSACTION,      // 대규모 거래
        RAPID_MOVEMENT,         // 빠른 자금 이동
        STRUCTURING,            // 구조화된 거래 (소액 분할)
        HIGH_RISK_COUNTRY,      // 고위험 국가
        UNUSUAL_PATTERN,        // 비정상 패턴
        PEP_MATCH,              // 정치적 주요 인물 매칭
        SANCTIONS_LIST          // 제재 리스트 매칭
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum AlertStatus {
        OPEN,           // 열림
        INVESTIGATING,  // 조사중
        RESOLVED,       // 해결됨
        FALSE_POSITIVE, // 오탐
        ESCALATED       // 상위 보고됨
    }
}
