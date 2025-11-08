package com.dkp.exchange.service;

import com.dkp.exchange.model.*;
import com.dkp.exchange.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 고급 수수료 계산 서비스
 * - VIP 등급별 할인 적용
 * - 거래소 토큰 보유량 기반 할인
 * - 동적 출금 수수료
 * - Fee 엔티티 기반 DB 수수료 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedFeeService {
    private final FeeRepository feeRepository;
    private final FeeDiscountRepository feeDiscountRepository;
    private final ExchangeSettingsRepository exchangeSettingsRepository;
    private final UserRepository userRepository;

    // 기본 수수료율 (Fee 엔티티가 없을 경우)
    private static final BigDecimal DEFAULT_MAKER_FEE = new BigDecimal("0.001"); // 0.1%
    private static final BigDecimal DEFAULT_TAKER_FEE = new BigDecimal("0.002"); // 0.2%

    /**
     * 통합 메이커 수수료 계산
     * 1. 기본 수수료 (Fee 엔티티 또는 ExchangeSettings)
     * 2. VIP 할인 적용
     * 3. 최종 수수료 반환
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateMakerFee(User user, String symbol, BigDecimal tradeAmount) {
        // 1. 기본 수수료율 조회
        BigDecimal baseFeeRate = getBaseMakerFeeRate(user, symbol);

        // 2. 기본 수수료 계산
        BigDecimal baseFee = tradeAmount.multiply(baseFeeRate).setScale(8, RoundingMode.HALF_UP);

        // 3. VIP 할인 적용
        BigDecimal finalFee = applyVipDiscount(user, baseFee, Fee.FeeType.MAKER);

        log.debug("Maker fee calculated for user {}: base={}, final={}", user.getId(), baseFee, finalFee);
        return finalFee;
    }

    /**
     * 통합 테이커 수수료 계산
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTakerFee(User user, String symbol, BigDecimal tradeAmount) {
        // 1. 기본 수수료율 조회
        BigDecimal baseFeeRate = getBaseTakerFeeRate(user, symbol);

        // 2. 기본 수수료 계산
        BigDecimal baseFee = tradeAmount.multiply(baseFeeRate).setScale(8, RoundingMode.HALF_UP);

        // 3. VIP 할인 적용
        BigDecimal finalFee = applyVipDiscount(user, baseFee, Fee.FeeType.TAKER);

        log.debug("Taker fee calculated for user {}: base={}, final={}", user.getId(), baseFee, finalFee);
        return finalFee;
    }

    /**
     * 동적 출금 수수료 계산 (Fee 엔티티 기반)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateWithdrawalFee(User user, String asset, BigDecimal amount) {
        // 1. Fee 엔티티에서 출금 수수료 조회
        Fee withdrawalFee = feeRepository
                .findBySymbolAndTypeAndActive(asset, Fee.FeeType.WITHDRAWAL, true)
                .orElse(null);

        BigDecimal baseFee;
        if (withdrawalFee != null) {
            baseFee = withdrawalFee.calculateFee(amount);
        } else {
            // Fee 엔티티가 없으면 하드코딩된 기본값 사용
            baseFee = getDefaultWithdrawalFee(asset);
        }

        // 2. VIP 할인 적용
        BigDecimal finalFee = applyVipDiscount(user, baseFee, Fee.FeeType.WITHDRAWAL);

        log.debug("Withdrawal fee calculated for user {}: asset={}, base={}, final={}",
                user.getId(), asset, baseFee, finalFee);
        return finalFee;
    }

    /**
     * 기본 메이커 수수료율 조회
     * 우선순위: Fee 엔티티 > ExchangeSettings > 기본값
     */
    private BigDecimal getBaseMakerFeeRate(User user, String symbol) {
        // 1순위: Fee 엔티티 (심볼별 글로벌 수수료)
        Fee fee = feeRepository
                .findBySymbolAndTypeAndActive(symbol, Fee.FeeType.MAKER, true)
                .orElse(null);

        if (fee != null) {
            return fee.getRate();
        }

        // 2순위: ExchangeSettings (사용자별 커스텀 수수료)
        ExchangeSettings settings = exchangeSettingsRepository.findByUserId(user.getId()).orElse(null);
        if (settings != null) {
            return settings.getMakerFee();
        }

        // 3순위: 기본값
        return DEFAULT_MAKER_FEE;
    }

    /**
     * 기본 테이커 수수료율 조회
     */
    private BigDecimal getBaseTakerFeeRate(User user, String symbol) {
        // 1순위: Fee 엔티티
        Fee fee = feeRepository
                .findBySymbolAndTypeAndActive(symbol, Fee.FeeType.TAKER, true)
                .orElse(null);

        if (fee != null) {
            return fee.getRate();
        }

        // 2순위: ExchangeSettings
        ExchangeSettings settings = exchangeSettingsRepository.findByUserId(user.getId()).orElse(null);
        if (settings != null) {
            return settings.getTakerFee();
        }

        // 3순위: 기본값
        return DEFAULT_TAKER_FEE;
    }

    /**
     * 기본 출금 수수료 (하드코딩 - 레거시)
     */
    private BigDecimal getDefaultWithdrawalFee(String asset) {
        return switch (asset.toUpperCase()) {
            case "BTC" -> new BigDecimal("0.0005");
            case "ETH" -> new BigDecimal("0.005");
            case "USDT" -> new BigDecimal("1.0");
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * VIP 등급별 할인 적용
     */
    private BigDecimal applyVipDiscount(User user, BigDecimal baseFee, Fee.FeeType feeType) {
        VipLevel vipLevel = user.getVipLevel();
        if (vipLevel == VipLevel.NONE) {
            return baseFee; // VIP 아니면 할인 없음
        }

        // FeeDiscount 엔티티에서 할인율 조회
        FeeDiscount discount = feeDiscountRepository
                .findByVipLevelAndActiveTrue(vipLevel)
                .orElse(null);

        if (discount == null) {
            // FeeDiscount 없으면 VipLevel enum의 기본 할인율 사용
            BigDecimal discountRate = vipLevel.getDiscountRate();
            BigDecimal discountedFee = baseFee.subtract(baseFee.multiply(discountRate));
            return discountedFee.max(BigDecimal.ZERO); // 음수 방지
        }

        // FeeDiscount 엔티티 사용
        return switch (feeType) {
            case MAKER -> discount.applyMakerDiscount(baseFee);
            case TAKER -> discount.applyTakerDiscount(baseFee);
            case WITHDRAWAL -> discount.applyWithdrawalDiscount(baseFee);
        };
    }

    /**
     * 수수료 할인 정보 조회
     */
    @Transactional(readOnly = true)
    public FeeDiscountInfo getFeeDiscountInfo(User user) {
        VipLevel vipLevel = user.getVipLevel();
        FeeDiscount discount = feeDiscountRepository
                .findByVipLevelAndActiveTrue(vipLevel)
                .orElse(null);

        return FeeDiscountInfo.builder()
                .vipLevel(vipLevel)
                .makerDiscount(discount != null ? discount.getMakerDiscount() : vipLevel.getDiscountRate())
                .takerDiscount(discount != null ? discount.getTakerDiscount() : vipLevel.getDiscountRate())
                .withdrawalDiscount(discount != null ? discount.getWithdrawalDiscount() : vipLevel.getDiscountRate())
                .build();
    }

    /**
     * 수수료 할인 정보 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class FeeDiscountInfo {
        private VipLevel vipLevel;
        private BigDecimal makerDiscount;
        private BigDecimal takerDiscount;
        private BigDecimal withdrawalDiscount;
    }

    /**
     * 주문이 메이커인지 테이커인지 판단
     */
    public boolean isMaker(Order order) {
        return order.getType() == Order.OrderType.LIMIT;
    }

    /**
     * 거래 수수료 계산 (메이커/테이커 자동 판단)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTradeFee(User user, Order order, String symbol, BigDecimal tradeAmount) {
        if (isMaker(order)) {
            return calculateMakerFee(user, symbol, tradeAmount);
        } else {
            return calculateTakerFee(user, symbol, tradeAmount);
        }
    }
}
