package com.dkp.exchange.service;

import com.dkp.exchange.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisOrderBookService {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String BUY_ORDERS_PREFIX = "orderbook:buy:";
    private static final String SELL_ORDERS_PREFIX = "orderbook:sell:";

    /**
     * 매수 주문을 Redis에 추가 (가격 높은 순)
     */
    public void addBuyOrder(String symbol, Order order) {
        String key = BUY_ORDERS_PREFIX + symbol;
        // Score를 음수로 저장하여 내림차순 정렬
        double score = -order.getPrice().doubleValue();
        redisTemplate.opsForZSet().add(key, orderToString(order), score);
    }

    /**
     * 매도 주문을 Redis에 추가 (가격 낮은 순)
     */
    public void addSellOrder(String symbol, Order order) {
        String key = SELL_ORDERS_PREFIX + symbol;
        // Score를 그대로 저장하여 오름차순 정렬
        double score = order.getPrice().doubleValue();
        redisTemplate.opsForZSet().add(key, orderToString(order), score);
    }

    /**
     * 매수 호가 조회 (상위 N개)
     */
    public List<OrderBookEntry> getBuyOrders(String symbol, int limit) {
        String key = BUY_ORDERS_PREFIX + symbol;
        Set<ZSetOperations.TypedTuple<Object>> tuples =
            redisTemplate.opsForZSet().rangeWithScores(key, 0, limit - 1);

        if (tuples == null) return Collections.emptyList();

        return tuples.stream()
            .map(tuple -> new OrderBookEntry(
                Math.abs(tuple.getScore()), // 음수를 다시 양수로
                parseQuantity(tuple.getValue())
            ))
            .collect(Collectors.toList());
    }

    /**
     * 매도 호가 조회 (상위 N개)
     */
    public List<OrderBookEntry> getSellOrders(String symbol, int limit) {
        String key = SELL_ORDERS_PREFIX + symbol;
        Set<ZSetOperations.TypedTuple<Object>> tuples =
            redisTemplate.opsForZSet().rangeWithScores(key, 0, limit - 1);

        if (tuples == null) return Collections.emptyList();

        return tuples.stream()
            .map(tuple -> new OrderBookEntry(
                tuple.getScore(),
                parseQuantity(tuple.getValue())
            ))
            .collect(Collectors.toList());
    }

    /**
     * 주문 제거
     */
    public void removeOrder(String symbol, Order order) {
        String key = order.getSide() == Order.OrderSide.BUY
            ? BUY_ORDERS_PREFIX + symbol
            : SELL_ORDERS_PREFIX + symbol;

        redisTemplate.opsForZSet().remove(key, orderToString(order));
    }

    /**
     * 심볼의 모든 주문 제거
     */
    public void clearOrderBook(String symbol) {
        redisTemplate.delete(BUY_ORDERS_PREFIX + symbol);
        redisTemplate.delete(SELL_ORDERS_PREFIX + symbol);
    }

    /**
     * 전체 호가창 조회
     */
    public OrderBookSnapshot getOrderBookSnapshot(String symbol, int depth) {
        return new OrderBookSnapshot(
            getBuyOrders(symbol, depth),
            getSellOrders(symbol, depth),
            System.currentTimeMillis()
        );
    }

    private String orderToString(Order order) {
        return order.getId() + ":" + order.getQuantity();
    }

    private double parseQuantity(Object value) {
        if (value == null) return 0;
        String str = value.toString();
        String[] parts = str.split(":");
        return parts.length > 1 ? Double.parseDouble(parts[1]) : 0;
    }

    // DTOs
    public record OrderBookEntry(double price, double quantity) {}

    public record OrderBookSnapshot(
        List<OrderBookEntry> bids,
        List<OrderBookEntry> asks,
        long timestamp
    ) {}
}
