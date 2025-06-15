package com.dkp.exchange.dto;

import lombok.Data;

@Data
public class PriceResponse {
    private String symbol;
    private double price;
    private double change;
    private double volume;

    public PriceResponse(String symbol, double price, double change, double volume) {
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.volume = volume;
    }
} 