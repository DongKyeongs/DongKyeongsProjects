package com.dkp.exchange.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Trade {
    private BigDecimal price;
    private BigDecimal amount;
    private String side;
    private long timestamp;
} 