package com.dkp.exchange.service;

import com.dkp.exchange.model.Order;
import com.dkp.exchange.model.User;
import com.dkp.exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserBalanceService {
    private final UserRepository userRepository;

    @Transactional
    public void updateBalances(Order buyOrder, Order sellOrder, BigDecimal quantity, BigDecimal price) {
        updateBalancesWithFee(buyOrder, sellOrder, quantity, price, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Transactional
    public void updateBalancesWithFee(Order buyOrder, Order sellOrder, BigDecimal quantity, BigDecimal price,
                                      BigDecimal buyerFee, BigDecimal sellerFee) {
        User buyer = buyOrder.getUser();
        User seller = sellOrder.getUser();

        BigDecimal totalAmount = quantity.multiply(price);
        String symbol = buyOrder.getSymbol();

        // 매수자: 코인 증가, USDT 감소 (거래 금액 + 수수료)
        updateUserAsset(buyer, symbol, quantity);
        buyer.setUsdtBalance(buyer.getUsdtBalance().subtract(totalAmount).subtract(buyerFee));

        // 매도자: 코인 감소, USDT 증가 (거래 금액 - 수수료)
        updateUserAsset(seller, symbol, quantity.negate());
        seller.setUsdtBalance(seller.getUsdtBalance().add(totalAmount).subtract(sellerFee));

        userRepository.save(buyer);
        userRepository.save(seller);
    }

    @Transactional
    public boolean checkBalance(Order order) {
        User user = order.getUser();
        BigDecimal totalAmount = order.getPrice().multiply(order.getQuantity());

        if (order.getSide() == Order.OrderSide.BUY) {
            // 매수 주문: USDT 잔고 확인
            return user.getUsdtBalance().compareTo(totalAmount) >= 0;
        } else {
            // 매도 주문: 코인 잔고 확인
            BigDecimal assetBalance = getUserAsset(user, order.getSymbol());
            return assetBalance.compareTo(order.getQuantity()) >= 0;
        }
    }

    private void updateUserAsset(User user, String symbol, BigDecimal amount) {
        switch (symbol.toUpperCase()) {
            case "BTC", "BTCUSDT" -> user.setBtcBalance(user.getBtcBalance().add(amount));
            case "ETH", "ETHUSDT" -> user.setEthBalance(user.getEthBalance().add(amount));
            default -> throw new RuntimeException("Unsupported symbol: " + symbol);
        }
    }

    private BigDecimal getUserAsset(User user, String symbol) {
        return switch (symbol.toUpperCase()) {
            case "BTC", "BTCUSDT" -> user.getBtcBalance();
            case "ETH", "ETHUSDT" -> user.getEthBalance();
            default -> throw new RuntimeException("Unsupported symbol: " + symbol);
        };
    }
} 