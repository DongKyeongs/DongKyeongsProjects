package com.dkp.exchange.dto;

import com.dkp.exchange.model.Order;
import com.dkp.exchange.model.OrderStatus;
import com.dkp.exchange.model.OrderType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private Long id;
    private String symbol;
    private OrderType type;
    private OrderStatus status;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal filledQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponse from(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setSymbol(order.getSymbol());
        response.setType(order.getType());
        response.setStatus(order.getStatus());
        response.setPrice(order.getPrice());
        response.setQuantity(order.getQuantity());
        response.setFilledQuantity(order.getFilledQuantity());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }
} 