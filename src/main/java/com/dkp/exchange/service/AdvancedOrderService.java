package com.dkp.exchange.service;

import com.dkp.exchange.model.AdvancedOrder;
import com.dkp.exchange.model.Order;
import com.dkp.exchange.model.User;
import com.dkp.exchange.repository.AdvancedOrderRepository;
import com.dkp.exchange.repository.OrderRepository;
import com.dkp.exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvancedOrderService {
    private final AdvancedOrderRepository advancedOrderRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final PriceService priceService;

    // OCO (One-Cancels-Other) 주문 생성
    @Transactional
    public AdvancedOrder createOCOOrder(Long userId, String symbol, Order.OrderSide side,
                                        BigDecimal stopPrice, BigDecimal limitPrice,
                                        BigDecimal quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AdvancedOrder ocoOrder = new AdvancedOrder();
        ocoOrder.setUser(user);
        ocoOrder.setType(AdvancedOrder.AdvancedOrderType.OCO);
        ocoOrder.setSymbol(symbol);
        ocoOrder.setSide(side);
        ocoOrder.setStopPrice(stopPrice);
        ocoOrder.setLimitPrice(limitPrice);
        ocoOrder.setTotalQuantity(quantity);
        ocoOrder.setStatus(AdvancedOrder.AdvancedOrderStatus.PENDING);

        AdvancedOrder saved = advancedOrderRepository.save(ocoOrder);
        log.info("OCO Order created: {} {} {} (Stop: {}, Limit: {})",
                side, quantity, symbol, stopPrice, limitPrice);

        return saved;
    }

    // Trailing Stop 주문 생성
    @Transactional
    public AdvancedOrder createTrailingStopOrder(Long userId, String symbol, Order.OrderSide side,
                                                  BigDecimal trailingPercent, BigDecimal quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 현재 가격 가져오기
        BigDecimal currentPrice = priceService.getCurrentPrice(symbol);

        AdvancedOrder trailingOrder = new AdvancedOrder();
        trailingOrder.setUser(user);
        trailingOrder.setType(AdvancedOrder.AdvancedOrderType.TRAILING_STOP);
        trailingOrder.setSymbol(symbol);
        trailingOrder.setSide(side);
        trailingOrder.setTrailingPercent(trailingPercent);
        trailingOrder.setTotalQuantity(quantity);
        trailingOrder.setStatus(AdvancedOrder.AdvancedOrderStatus.PENDING);

        // 초기 최고/최저가 설정
        if (side == Order.OrderSide.SELL) {
            trailingOrder.setHighestPrice(currentPrice);
        } else {
            trailingOrder.setLowestPrice(currentPrice);
        }

        AdvancedOrder saved = advancedOrderRepository.save(trailingOrder);
        log.info("Trailing Stop Order created: {} {} {} (Trailing: {}%)",
                side, quantity, symbol, trailingPercent);

        return saved;
    }

    // Iceberg 주문 생성
    @Transactional
    public AdvancedOrder createIcebergOrder(Long userId, String symbol, Order.OrderSide side,
                                            BigDecimal price, BigDecimal totalQuantity,
                                            BigDecimal visibleQuantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (visibleQuantity.compareTo(totalQuantity) > 0) {
            throw new RuntimeException("Visible quantity cannot exceed total quantity");
        }

        AdvancedOrder icebergOrder = new AdvancedOrder();
        icebergOrder.setUser(user);
        icebergOrder.setType(AdvancedOrder.AdvancedOrderType.ICEBERG);
        icebergOrder.setSymbol(symbol);
        icebergOrder.setSide(side);
        icebergOrder.setLimitPrice(price);
        icebergOrder.setTotalQuantity(totalQuantity);
        icebergOrder.setVisibleQuantity(visibleQuantity);
        icebergOrder.setFilledQuantity(BigDecimal.ZERO);
        icebergOrder.setStatus(AdvancedOrder.AdvancedOrderStatus.PENDING);

        // 첫 번째 일반 주문 생성 (보이는 수량만)
        createVisibleOrder(icebergOrder);

        AdvancedOrder saved = advancedOrderRepository.save(icebergOrder);
        log.info("Iceberg Order created: {} {} {} (Total: {}, Visible: {})",
                side, price, symbol, totalQuantity, visibleQuantity);

        return saved;
    }

    // OCO 주문 모니터링 (스케줄러)
    @Scheduled(fixedDelay = 1000) // 1초마다 체크
    @Transactional
    public void monitorOCOOrders() {
        List<AdvancedOrder> pendingOCOs = advancedOrderRepository
                .findPendingByType(AdvancedOrder.AdvancedOrderType.OCO);

        for (AdvancedOrder oco : pendingOCOs) {
            try {
                BigDecimal currentPrice = priceService.getCurrentPrice(oco.getSymbol());

                // Stop Price 트리거 체크
                if (shouldTriggerStop(oco, currentPrice)) {
                    triggerStopOrder(oco, currentPrice);
                }
                // Limit Price 트리거 체크
                else if (shouldTriggerLimit(oco, currentPrice)) {
                    triggerLimitOrder(oco);
                }
            } catch (Exception e) {
                log.error("Error monitoring OCO order {}: {}", oco.getId(), e.getMessage());
            }
        }
    }

    // Trailing Stop 모니터링
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void monitorTrailingStops() {
        List<AdvancedOrder> pendingTrailing = advancedOrderRepository
                .findPendingByType(AdvancedOrder.AdvancedOrderType.TRAILING_STOP);

        for (AdvancedOrder trailing : pendingTrailing) {
            try {
                BigDecimal currentPrice = priceService.getCurrentPrice(trailing.getSymbol());
                updateTrailingStop(trailing, currentPrice);
            } catch (Exception e) {
                log.error("Error monitoring trailing stop {}: {}", trailing.getId(), e.getMessage());
            }
        }
    }

    private void updateTrailingStop(AdvancedOrder trailing, BigDecimal currentPrice) {
        if (trailing.getSide() == Order.OrderSide.SELL) {
            // 매도 Trailing Stop: 최고가 갱신
            if (currentPrice.compareTo(trailing.getHighestPrice()) > 0) {
                trailing.setHighestPrice(currentPrice);
                advancedOrderRepository.save(trailing);
            }

            // Trailing 퍼센트만큼 하락하면 트리거
            BigDecimal triggerPrice = trailing.getHighestPrice()
                    .multiply(BigDecimal.ONE.subtract(trailing.getTrailingPercent().divide(new BigDecimal("100"))));

            if (currentPrice.compareTo(triggerPrice) <= 0) {
                triggerTrailingStop(trailing, currentPrice);
            }
        } else {
            // 매수 Trailing Stop: 최저가 갱신
            if (currentPrice.compareTo(trailing.getLowestPrice()) < 0) {
                trailing.setLowestPrice(currentPrice);
                advancedOrderRepository.save(trailing);
            }

            // Trailing 퍼센트만큼 상승하면 트리거
            BigDecimal triggerPrice = trailing.getLowestPrice()
                    .multiply(BigDecimal.ONE.add(trailing.getTrailingPercent().divide(new BigDecimal("100"))));

            if (currentPrice.compareTo(triggerPrice) >= 0) {
                triggerTrailingStop(trailing, currentPrice);
            }
        }
    }

    private boolean shouldTriggerStop(AdvancedOrder oco, BigDecimal currentPrice) {
        if (oco.getSide() == Order.OrderSide.SELL) {
            return currentPrice.compareTo(oco.getStopPrice()) <= 0;
        } else {
            return currentPrice.compareTo(oco.getStopPrice()) >= 0;
        }
    }

    private boolean shouldTriggerLimit(AdvancedOrder oco, BigDecimal currentPrice) {
        if (oco.getSide() == Order.OrderSide.SELL) {
            return currentPrice.compareTo(oco.getLimitPrice()) >= 0;
        } else {
            return currentPrice.compareTo(oco.getLimitPrice()) <= 0;
        }
    }

    private void triggerStopOrder(AdvancedOrder oco, BigDecimal currentPrice) {
        // Stop 주문 생성 (시장가)
        Order stopOrder = new Order();
        stopOrder.setUser(oco.getUser());
        stopOrder.setSymbol(oco.getSymbol());
        stopOrder.setType(Order.OrderType.MARKET);
        stopOrder.setSide(oco.getSide());
        stopOrder.setPrice(currentPrice);
        stopOrder.setQuantity(oco.getTotalQuantity());

        orderRepository.save(stopOrder);
        oco.setPrimaryOrder(stopOrder);
        oco.setStatus(AdvancedOrder.AdvancedOrderStatus.TRIGGERED);
        oco.setTriggeredAt(LocalDateTime.now());
        advancedOrderRepository.save(oco);

        log.info("OCO Stop triggered: {} at {}", oco.getId(), currentPrice);
    }

    private void triggerLimitOrder(AdvancedOrder oco) {
        // Limit 주문 생성
        Order limitOrder = new Order();
        limitOrder.setUser(oco.getUser());
        limitOrder.setSymbol(oco.getSymbol());
        limitOrder.setType(Order.OrderType.LIMIT);
        limitOrder.setSide(oco.getSide());
        limitOrder.setPrice(oco.getLimitPrice());
        limitOrder.setQuantity(oco.getTotalQuantity());

        orderRepository.save(limitOrder);
        oco.setPrimaryOrder(limitOrder);
        oco.setStatus(AdvancedOrder.AdvancedOrderStatus.TRIGGERED);
        oco.setTriggeredAt(LocalDateTime.now());
        advancedOrderRepository.save(oco);

        log.info("OCO Limit triggered: {} at {}", oco.getId(), oco.getLimitPrice());
    }

    private void triggerTrailingStop(AdvancedOrder trailing, BigDecimal currentPrice) {
        Order marketOrder = new Order();
        marketOrder.setUser(trailing.getUser());
        marketOrder.setSymbol(trailing.getSymbol());
        marketOrder.setType(Order.OrderType.MARKET);
        marketOrder.setSide(trailing.getSide());
        marketOrder.setPrice(currentPrice);
        marketOrder.setQuantity(trailing.getTotalQuantity());

        orderRepository.save(marketOrder);
        trailing.setPrimaryOrder(marketOrder);
        trailing.setStatus(AdvancedOrder.AdvancedOrderStatus.TRIGGERED);
        trailing.setTriggeredAt(LocalDateTime.now());
        advancedOrderRepository.save(trailing);

        log.info("Trailing Stop triggered: {} at {}", trailing.getId(), currentPrice);
    }

    private void createVisibleOrder(AdvancedOrder iceberg) {
        BigDecimal remainingQty = iceberg.getTotalQuantity().subtract(iceberg.getFilledQuantity());
        BigDecimal orderQty = remainingQty.min(iceberg.getVisibleQuantity());

        Order visibleOrder = new Order();
        visibleOrder.setUser(iceberg.getUser());
        visibleOrder.setSymbol(iceberg.getSymbol());
        visibleOrder.setType(Order.OrderType.LIMIT);
        visibleOrder.setSide(iceberg.getSide());
        visibleOrder.setPrice(iceberg.getLimitPrice());
        visibleOrder.setQuantity(orderQty);

        orderRepository.save(visibleOrder);
        iceberg.setPrimaryOrder(visibleOrder);
    }

    @Transactional
    public void cancelAdvancedOrder(Long orderId, Long userId) {
        AdvancedOrder order = advancedOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Advanced order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // 연관된 일반 주문들도 취소
        if (order.getPrimaryOrder() != null && order.getPrimaryOrder().getStatus() == Order.OrderStatus.PENDING) {
            orderService.cancelOrder(order.getPrimaryOrder().getId());
        }

        order.setStatus(AdvancedOrder.AdvancedOrderStatus.CANCELLED);
        advancedOrderRepository.save(order);

        log.info("Advanced order cancelled: {}", orderId);
    }
}
