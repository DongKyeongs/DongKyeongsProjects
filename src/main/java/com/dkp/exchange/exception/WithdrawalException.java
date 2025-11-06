package com.dkp.exchange.exception;

import org.springframework.http.HttpStatus;

public class WithdrawalException extends ExchangeException {
    public WithdrawalException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "WITHDRAWAL_ERROR");
    }
}
