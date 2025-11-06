package com.dkp.exchange.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends ExchangeException {
    public UnauthorizedAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS");
    }
}
