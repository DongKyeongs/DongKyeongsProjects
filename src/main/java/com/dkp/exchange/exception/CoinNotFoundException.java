package com.dkp.exchange.exception;

import org.springframework.http.HttpStatus;

public class CoinNotFoundException extends ExchangeException {
    public CoinNotFoundException(String symbol) {
        super("Coin not found: " + symbol, HttpStatus.NOT_FOUND, "COIN_NOT_FOUND");
    }
}
