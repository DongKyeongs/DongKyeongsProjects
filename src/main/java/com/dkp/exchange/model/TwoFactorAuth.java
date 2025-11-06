package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "two_factor_auth")
public class TwoFactorAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String secret; // TOTP secret (암호화되어 저장)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TwoFactorType type = TwoFactorType.TOTP;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false)
    private boolean verified = false;

    @Column
    private String backupCodes; // 백업 코드 (암호화, 쉼표로 구분)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastVerifiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TwoFactorType {
        TOTP,  // Google Authenticator 등
        SMS,   // SMS OTP
        EMAIL  // Email OTP
    }
}
