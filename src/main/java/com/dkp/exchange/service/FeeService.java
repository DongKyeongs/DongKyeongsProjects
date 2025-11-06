package com.dkp.exchange.service;

import com.dkp.exchange.model.ExchangeSettings;
import com.dkp.exchange.model.Order;
import com.dkp.exchange.model.User;
import com.dkp.exchange.repository.ExchangeSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class FeeService {
    private final ExchangeSettingsRepository exchangeSettingsRepository;

    // 기본 수수료율
    private static final BigDecimal DEFAULT_MAKER_FEE = new BigDecimal("0.001"); // 0.1%
    private static final BigDecimal DEFAULT_TAKER_FEE = new BigDecimal("0.002"); // 0.2%

    /**
     * 메이커 수수료 계산
     * 메이커: 호가창에 주문을 올려 유동성을 제공하는 사용자
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateMakerFee(User user, BigDecimal tradeAmount) {
        BigDecimal feeRate = getMakerFeeRate(user);
        return tradeAmount.multiply(feeRate).setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * 테이커 수수료 계산
     * 테이커: 호가창의 주문을 체결하여 유동성을 가져가는 사용자
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTakerFee(User user, BigDecimal tradeAmount) {
        BigDecimal feeRate = getTakerFeeRate(user);
        return tradeAmount.multiply(feeRate).setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * 출금 수수료 계산
     */
    public BigDecimal calculateWithdrawalFee(String asset, BigDecimal amount) {
        // 자산별 출금 수수료 (실제로는 DB에서 관리)
        return switch (asset.toUpperCase()) {
            case "BTC" -> new BigDecimal("0.0005"); // 0.0005 BTC
            case "ETH" -> new BigDecimal("0.005");  // 0.005 ETH
            case "USDT" -> new BigDecimal("1.0");   // 1 USDT
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 사용자의 메이커 수수료율 조회
     */
    @Transactional(readOnly = true)
    public BigDecimal getMakerFeeRate(User user) {
        return exchangeSettingsRepository.findByUserId(user.getId())
                .map(ExchangeSettings::getMakerFee)
                .orElse(DEFAULT_MAKER_FEE);
    }

    /**
     * 사용자의 테이커 수수료율 조회
     */
    @Transactional(readOnly = true)
    public BigDecimal getTakerFeeRate(User user) {
        return exchangeSettingsRepository.findByUserId(user.getId())
                .map(ExchangeSettings::getTakerFee)
                .orElse(DEFAULT_TAKER_FEE);
    }

    /**
     * 주문이 메이커인지 테이커인지 판단
     * 실제로는 OrderMatchingEngine에서 판단
     */
    public boolean isMaker(Order order) {
        // 지정가 주문이 즉시 체결되지 않고 호가창에 등록되면 메이커
        // 시장가 주문이나 즉시 체결되는 주문은 테이커
        return order.getType() == Order.OrderType.LIMIT;
    }

    /**
     * 거래 수수료 계산 (메이커/테이커 자동 판단)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTradeFee(User user, Order order, BigDecimal tradeAmount) {
        if (isMaker(order)) {
            return calculateMakerFee(user, tradeAmount);
        } else {
            return calculateTakerFee(user, tradeAmount);
        }
    }

    /**
     * 수수료 할인율 적용 (VIP 등급, 거래량 기반)
     */
    public BigDecimal applyDiscount(BigDecimal fee, User user) {
        // TODO: VIP 등급이나 거래량에 따른 할인율 적용
        // 예: VIP 사용자는 50% 할인
        return fee;
    }
}
