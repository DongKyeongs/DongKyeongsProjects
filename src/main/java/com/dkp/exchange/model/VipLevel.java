package com.dkp.exchange.model;

public enum VipLevel {
    NONE(0, "0", "0"),           // 일반 회원: 할인 없음
    VIP1(1, "100000", "10"),     // VIP 1: 30일 거래량 $100,000+, 10% 할인
    VIP2(2, "500000", "20"),     // VIP 2: 30일 거래량 $500,000+, 20% 할인
    VIP3(3, "1000000", "30"),    // VIP 3: 30일 거래량 $1,000,000+, 30% 할인
    VIP4(4, "5000000", "40"),    // VIP 4: 30일 거래량 $5,000,000+, 40% 할인
    VIP5(5, "10000000", "50");   // VIP 5: 30일 거래량 $10,000,000+, 50% 할인

    private final int level;
    private final String requiredVolume;  // 30일 거래량 요구사항 (USDT)
    private final String discountPercent; // 수수료 할인율 (%)

    VipLevel(int level, String requiredVolume, String discountPercent) {
        this.level = level;
        this.requiredVolume = requiredVolume;
        this.discountPercent = discountPercent;
    }

    public int getLevel() {
        return level;
    }

    public String getRequiredVolume() {
        return requiredVolume;
    }

    public String getDiscountPercent() {
        return discountPercent;
    }

    /**
     * 거래량에 따라 적절한 VIP 등급 반환
     */
    public static VipLevel calculateVipLevel(java.math.BigDecimal tradingVolume) {
        if (tradingVolume.compareTo(new java.math.BigDecimal("10000000")) >= 0) {
            return VIP5;
        } else if (tradingVolume.compareTo(new java.math.BigDecimal("5000000")) >= 0) {
            return VIP4;
        } else if (tradingVolume.compareTo(new java.math.BigDecimal("1000000")) >= 0) {
            return VIP3;
        } else if (tradingVolume.compareTo(new java.math.BigDecimal("500000")) >= 0) {
            return VIP2;
        } else if (tradingVolume.compareTo(new java.math.BigDecimal("100000")) >= 0) {
            return VIP1;
        } else {
            return NONE;
        }
    }

    /**
     * 수수료 할인율 반환 (0.1 = 10% 할인)
     */
    public java.math.BigDecimal getDiscountRate() {
        return new java.math.BigDecimal(discountPercent).divide(new java.math.BigDecimal("100"));
    }
}
