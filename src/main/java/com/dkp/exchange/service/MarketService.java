package com.dkp.exchange.service;

import com.dkp.exchange.model.Coin;
import com.dkp.exchange.model.OrderBook;
import com.dkp.exchange.model.Order;
import com.dkp.exchange.model.OrderStatus;
import com.dkp.exchange.repository.CoinRepository;
import com.dkp.exchange.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketService {
    private final CoinRepository coinRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<Coin> getAllCoins() {
        return coinRepository.findAll().stream()
                .filter(Coin::isActive)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Coin getCoin(String symbol) {
        Coin coin = coinRepository.findBySymbol(symbol);
        if (coin == null) {
            throw new RuntimeException("Coin not found: " + symbol);
        }
        return coin;
    }

    @Transactional(readOnly = true)
    public OrderBook getOrderBook(String symbol) {
        // 대기 중인 주문들을 가져와서 OrderBook 생성
        List<Order> pendingOrders = orderRepository.findBySymbolAndStatus(symbol, OrderStatus.PENDING);

        // 실제로는 OrderBook 객체를 생성하고 매수/매도 주문을 분리해야 함
        // 여기서는 간단하게 구현
        OrderBook orderBook = new OrderBook();
        orderBook.setSymbol(symbol);
        // OrderBook의 필드에 따라 추가 구현 필요

        return orderBook;
    }

    @Transactional
    public Coin updateCoinPrice(String symbol, BigDecimal newPrice) {
        Coin coin = coinRepository.findBySymbol(symbol);
        if (coin == null) {
            throw new RuntimeException("Coin not found: " + symbol);
        }

        BigDecimal priceChange = newPrice.subtract(coin.getCurrentPrice());
        BigDecimal priceChangePercent = priceChange
                .divide(coin.getCurrentPrice(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        coin.setCurrentPrice(newPrice);
        coin.setPriceChange24h(priceChangePercent);

        return coinRepository.save(coin);
    }

    @Transactional
    public Coin createCoin(Coin coin) {
        if (coinRepository.findBySymbol(coin.getSymbol()) != null) {
            throw new RuntimeException("Coin already exists: " + coin.getSymbol());
        }
        return coinRepository.save(coin);
    }

    @Transactional
    public void deactivateCoin(String symbol) {
        Coin coin = coinRepository.findBySymbol(symbol);
        if (coin == null) {
            throw new RuntimeException("Coin not found: " + symbol);
        }
        coin.setActive(false);
        coinRepository.save(coin);
    }
}
