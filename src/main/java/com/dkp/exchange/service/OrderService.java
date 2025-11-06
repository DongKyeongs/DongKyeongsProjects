package com.dkp.exchange.service;

import com.dkp.exchange.dto.OrderRequest;
import com.dkp.exchange.dto.OrderResponse;
import com.dkp.exchange.model.Order;
import com.dkp.exchange.model.OrderStatus;
import com.dkp.exchange.model.User;
import com.dkp.exchange.repository.OrderRepository;
import com.dkp.exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public Order createOrder(OrderRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setSymbol(request.getSymbol());
        order.setType(request.getType());
        order.setSide(request.getSide());
        order.setPrice(request.getPrice());
        order.setQuantity(request.getQuantity());
        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String symbol, String status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders;

        if (symbol != null && status != null) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            orders = orderRepository.findByUserIdAndStatus(user.getId(), orderStatus).stream()
                    .filter(o -> o.getSymbol().equals(symbol))
                    .collect(Collectors.toList());
        } else if (status != null) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            orders = orderRepository.findByUserIdAndStatus(user.getId(), orderStatus);
        } else if (symbol != null) {
            orders = orderRepository.findByUserIdAndSymbol(user.getId(), symbol);
        } else {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }

        return OrderResponse.from(order);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }

        if (order.getStatus() == OrderStatus.FILLED) {
            throw new RuntimeException("Cannot cancel filled order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public OrderResponse modifyOrder(Long id, BigDecimal newPrice, BigDecimal newQuantity) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }

        if (order.getStatus() == OrderStatus.FILLED) {
            throw new RuntimeException("Cannot modify filled order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot modify cancelled order");
        }

        // 부분 체결된 주문의 경우, 남은 수량만 수정 가능
        if (order.getStatus() == OrderStatus.PARTIAL) {
            BigDecimal remainingQuantity = order.getQuantity().subtract(order.getFilledQuantity());
            if (newQuantity.compareTo(remainingQuantity) < 0) {
                throw new RuntimeException("New quantity cannot be less than remaining quantity");
            }
        }

        // 가격/수량 수정
        if (newPrice != null) {
            order.setPrice(newPrice);
        }
        if (newQuantity != null) {
            order.setQuantity(newQuantity);
        }

        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.from(updatedOrder);
    }

    @Transactional
    public void cancelAllOrders(String symbol) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders;
        if (symbol != null) {
            orders = orderRepository.findByUserIdAndSymbol(user.getId(), symbol);
        } else {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.PARTIAL)
                .forEach(o -> {
                    o.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(o);
                });
    }
}
