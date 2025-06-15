package com.dkp.exchange.controller;

import com.dkp.exchange.dto.OrderRequest;
import com.dkp.exchange.dto.OrderResponse;
import com.dkp.exchange.model.Order;
import com.dkp.exchange.service.OrderMatchingEngine;
import com.dkp.exchange.service.OrderService;
import com.dkp.exchange.service.UserBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderMatchingEngine matchingEngine;
    private final UserBalanceService userBalanceService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        // 주문 생성
        Order order = orderService.createOrder(request);

        // 잔고 확인
        if (!userBalanceService.checkBalance(order)) {
            return ResponseEntity.badRequest().build();
        }

        // 주문 매칭 엔진에 전달
        matchingEngine.processOrder(order);

        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.getOrders(symbol, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }
} 