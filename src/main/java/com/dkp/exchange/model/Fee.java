package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "fees")
public class Fee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeeType type;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate; // 수수료율 (예: 0.001 = 0.1%)

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal minFee = BigDecimal.ZERO; // 최소 수수료

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal maxFee = new BigDecimal("999999"); // 최대 수수료

    @Column(nullable = false)
    private boolean active = true;

    public enum FeeType {
        MAKER,      // 메이커 수수료 (유동성 제공자)
        TAKER,      // 테이커 수수료 (유동성 소비자)
        WITHDRAWAL  // 출금 수수료
    }

    public BigDecimal calculateFee(BigDecimal amount) {
        BigDecimal fee = amount.multiply(rate);

        // 최소/최대 수수료 적용
        if (fee.compareTo(minFee) < 0) {
            fee = minFee;
        }
        if (fee.compareTo(maxFee) > 0) {
            fee = maxFee;
        }

        return fee;
    }
}
