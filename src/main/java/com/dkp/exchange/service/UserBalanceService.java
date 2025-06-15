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
        User buyer = buyOrder.getUser();
        User seller = sellOrder.getUser();
        
        BigDecimal totalAmount = quantity.multiply(price);

        // 매수자: 코인 증가, USDT 감소
        buyer.setBalance(buyer.getBalance().add(quantity));
        buyer.setUsdtBalance(buyer.getUsdtBalance().subtract(totalAmount));

        // 매도자: 코인 감소, USDT 증가
        seller.setBalance(seller.getBalance().subtract(quantity));
        seller.setUsdtBalance(seller.getUsdtBalance().add(totalAmount));

        userRepository.save(buyer);
        userRepository.save(seller);
    }

    @Transactional
    public boolean checkBalance(Order order) {
        User user = order.getUser();
        BigDecimal totalAmount = order.getPrice().multiply(order.getQuantity());

        if (order.getType() == OrderType.BUY) {
            // 매수 주문: USDT 잔고 확인
            return user.getUsdtBalance().compareTo(totalAmount) >= 0;
        } else {
            // 매도 주문: 코인 잔고 확인
            return user.getBalance().compareTo(order.getQuantity()) >= 0;
        }
    }
} 