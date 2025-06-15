package com.dkp.exchange.service;

import com.dkp.exchange.model.Order;
import com.dkp.exchange.model.OrderStatus;
import com.dkp.exchange.model.OrderType;
import com.dkp.exchange.model.Trade;
import com.dkp.exchange.repository.OrderRepository;
import com.dkp.exchange.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

@Service
@RequiredArgsConstructor
public class OrderMatchingEngine {
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final UserBalanceService userBalanceService;

    // 매수 주문 큐 (가격 높은 순)
    private final Queue<Order> buyOrders = new PriorityQueue<>(
        Comparator.comparing(Order::getPrice).reversed()
    );

    // 매도 주문 큐 (가격 낮은 순)
    private final Queue<Order> sellOrders = new PriorityQueue<>(
        Comparator.comparing(Order::getPrice)
    );

    @Transactional
    public void processOrder(Order order) {
        if (order.getType() == OrderType.BUY) {
            processBuyOrder(order);
        } else {
            processSellOrder(order);
        }
    }

    private void processBuyOrder(Order buyOrder) {
        while (!sellOrders.isEmpty() && buyOrder.getStatus() != OrderStatus.COMPLETED) {
            Order sellOrder = sellOrders.peek();
            
            // 매수가가 매도가보다 낮으면 매칭 불가
            if (buyOrder.getPrice().compareTo(sellOrder.getPrice()) < 0) {
                break;
            }

            // 체결 가능한 수량 계산
            BigDecimal matchQuantity = buyOrder.getQuantity()
                .subtract(buyOrder.getFilledQuantity())
                .min(sellOrder.getQuantity().subtract(sellOrder.getFilledQuantity()));

            if (matchQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // 체결 처리
            executeTrade(buyOrder, sellOrder, matchQuantity, sellOrder.getPrice());
        }

        // 남은 수량이 있으면 매수 주문 큐에 추가
        if (buyOrder.getStatus() == OrderStatus.PENDING) {
            buyOrders.add(buyOrder);
        }
    }

    private void processSellOrder(Order sellOrder) {
        while (!buyOrders.isEmpty() && sellOrder.getStatus() != OrderStatus.COMPLETED) {
            Order buyOrder = buyOrders.peek();
            
            // 매도가가 매수가보다 높으면 매칭 불가
            if (sellOrder.getPrice().compareTo(buyOrder.getPrice()) > 0) {
                break;
            }

            // 체결 가능한 수량 계산
            BigDecimal matchQuantity = sellOrder.getQuantity()
                .subtract(sellOrder.getFilledQuantity())
                .min(buyOrder.getQuantity().subtract(buyOrder.getFilledQuantity()));

            if (matchQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // 체결 처리
            executeTrade(buyOrder, sellOrder, matchQuantity, buyOrder.getPrice());
        }

        // 남은 수량이 있으면 매도 주문 큐에 추가
        if (sellOrder.getStatus() == OrderStatus.PENDING) {
            sellOrders.add(sellOrder);
        }
    }

    private void executeTrade(Order buyOrder, Order sellOrder, BigDecimal quantity, BigDecimal price) {
        // 체결 내역 생성
        Trade trade = new Trade();
        trade.setSymbol(buyOrder.getSymbol());
        trade.setBuyOrder(buyOrder);
        trade.setSellOrder(sellOrder);
        trade.setPrice(price);
        trade.setQuantity(quantity);
        tradeRepository.save(trade);

        // 주문 상태 업데이트
        updateOrderStatus(buyOrder, quantity);
        updateOrderStatus(sellOrder, quantity);

        // 사용자 잔고 업데이트
        userBalanceService.updateBalances(buyOrder, sellOrder, quantity, price);
    }

    private void updateOrderStatus(Order order, BigDecimal filledQuantity) {
        order.setFilledQuantity(order.getFilledQuantity().add(filledQuantity));
        
        if (order.getFilledQuantity().compareTo(order.getQuantity()) >= 0) {
            order.setStatus(OrderStatus.COMPLETED);
        } else {
            order.setStatus(OrderStatus.PARTIAL);
        }
        
        orderRepository.save(order);
    }
} 