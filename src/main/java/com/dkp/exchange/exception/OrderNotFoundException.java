package com.dkp.exchange.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends ExchangeException {
    public OrderNotFoundException(Long orderId) {
        super("Order not found: " + orderId, HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND");
    }
}
