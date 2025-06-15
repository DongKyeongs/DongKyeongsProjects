package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private BigDecimal usdtBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal btcBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal ethBalance = BigDecimal.ZERO;
} 