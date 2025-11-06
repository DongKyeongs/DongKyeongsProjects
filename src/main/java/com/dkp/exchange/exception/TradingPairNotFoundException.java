package com.dkp.exchange.exception;

import org.springframework.http.HttpStatus;

public class TradingPairNotFoundException extends ExchangeException {
    public TradingPairNotFoundException(String pair) {
        super("Trading pair not found: " + pair, HttpStatus.NOT_FOUND, "TRADING_PAIR_NOT_FOUND");
    }
}
