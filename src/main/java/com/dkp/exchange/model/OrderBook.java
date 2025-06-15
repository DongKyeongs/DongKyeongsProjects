package com.dkp.exchange.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.TreeMap;

@Data
public class OrderBook {
    private TreeMap<BigDecimal, BigDecimal> bids = new TreeMap<>(Collections.reverseOrder());
    private TreeMap<BigDecimal, BigDecimal> asks = new TreeMap<>();
} 