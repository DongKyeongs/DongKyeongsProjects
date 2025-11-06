package com.dkp.exchange.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends ExchangeException {
    public RateLimitExceededException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
    }
}
