package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "kyc_verifications")
public class KycVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycLevel level = KycLevel.UNVERIFIED;

    // 개인 정보
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String nationality;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String postalCode;

    // 신원 확인 문서
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String documentNumber;

    @Column
    private String documentFrontImageUrl; // 신분증 앞면

    @Column
    private String documentBackImageUrl;  // 신분증 뒷면

    @Column
    private String selfieImageUrl;        // 셀카

    @Column
    private String proofOfAddressUrl;     // 주소 증명 서류

    // 검증 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime verifiedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        submittedAt = LocalDateTime.now();
    }

    public enum KycLevel {
        UNVERIFIED,  // 미인증 - 거래 불가
        TIER1,       // 티어1 - 입금만 가능, 일일 출금 한도 $1,000
        TIER2,       // 티어2 - 일일 출금 한도 $50,000
        TIER3        // 티어3 - 무제한 거래
    }

    public enum DocumentType {
        PASSPORT,         // 여권
        ID_CARD,          // 신분증
        DRIVERS_LICENSE   // 운전면허증
    }

    public enum VerificationStatus {
        PENDING,    // 대기중
        REVIEWING,  // 검토중
        APPROVED,   // 승인됨
        REJECTED    // 거부됨
    }
}
