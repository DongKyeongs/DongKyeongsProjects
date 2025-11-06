package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "api_keys")
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String apiKey;

    @Column(nullable = false)
    private String secretKey; // 암호화되어 저장

    @Column(nullable = false)
    private String name; // API 키 이름/설명

    @Column(nullable = false)
    private boolean canRead = true;

    @Column(nullable = false)
    private boolean canTrade = false;

    @Column(nullable = false)
    private boolean canWithdraw = false;

    @Column
    private String ipWhitelist; // 쉼표로 구분된 IP 목록

    @Column(nullable = false)
    private boolean active = true;

    @Column
    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (apiKey == null) {
            apiKey = "ak_" + UUID.randomUUID().toString().replace("-", "");
        }
        if (secretKey == null) {
            secretKey = "sk_" + UUID.randomUUID().toString().replace("-", "");
        }
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isIpAllowed(String ip) {
        if (ipWhitelist == null || ipWhitelist.isEmpty()) {
            return true; // 화이트리스트 없으면 모든 IP 허용
        }
        String[] allowedIps = ipWhitelist.split(",");
        for (String allowedIp : allowedIps) {
            if (allowedIp.trim().equals(ip)) {
                return true;
            }
        }
        return false;
    }
}
