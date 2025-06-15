package com.dkp.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateDTO {
    private String symbol;
    private BigDecimal price;
    private BigDecimal volume24h;
    private BigDecimal priceChange24h;
    private LocalDateTime timestamp;
} 