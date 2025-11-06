package com.dkp.exchange.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExchangeException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public ExchangeException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public ExchangeException(String message, HttpStatus status) {
        this(message, status, status.name());
    }
}
