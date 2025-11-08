package com.dkp.exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * VIP 등급별 수수료 할인 정책
 * 관리자가 VIP 등급별 할인율을 설정할 수 있음
 */
@Data
@Entity
@Table(name = "fee_discounts")
public class FeeDiscount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private VipLevel vipLevel;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal makerDiscount = BigDecimal.ZERO; // 메이커 수수료 할인율 (0.1 = 10%)

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal takerDiscount = BigDecimal.ZERO; // 테이커 수수료 할인율 (0.1 = 10%)

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal withdrawalDiscount = BigDecimal.ZERO; // 출금 수수료 할인율 (0.1 = 10%)

    @Column(nullable = false)
    private boolean active = true;

    @Column
    private String description; // 할인 정책 설명

    /**
     * 수수료에 할인 적용
     */
    public BigDecimal applyMakerDiscount(BigDecimal fee) {
        if (!active) return fee;
        return fee.subtract(fee.multiply(makerDiscount));
    }

    public BigDecimal applyTakerDiscount(BigDecimal fee) {
        if (!active) return fee;
        return fee.subtract(fee.multiply(takerDiscount));
    }

    public BigDecimal applyWithdrawalDiscount(BigDecimal fee) {
        if (!active) return fee;
        return fee.subtract(fee.multiply(withdrawalDiscount));
    }
}
