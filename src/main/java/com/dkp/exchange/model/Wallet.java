package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wallets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "asset"})
})
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String asset; // BTC, ETH, USDT

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal lockedBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private String address; // 지갑 주소

    @Column
    private String memo; // 메모 (XRP, XLM 등에 필요)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BigDecimal getAvailableBalance() {
        return balance.subtract(lockedBalance);
    }

    public void lock(BigDecimal amount) {
        if (getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance to lock");
        }
        lockedBalance = lockedBalance.add(amount);
    }

    public void unlock(BigDecimal amount) {
        if (lockedBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient locked balance to unlock");
        }
        lockedBalance = lockedBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance to debit");
        }
        balance = balance.subtract(amount);
    }
}
