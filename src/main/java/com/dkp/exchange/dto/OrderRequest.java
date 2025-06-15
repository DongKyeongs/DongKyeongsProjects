package com.dkp.exchange.dto;

import com.dkp.exchange.model.OrderType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {
    private String symbol;
    private OrderType type;
    private BigDecimal price;
    private BigDecimal quantity;
} 