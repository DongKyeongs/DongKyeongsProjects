package com.dkp.exchange.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ExchangeException {
    public UserNotFoundException(String username) {
        super("User not found: " + username, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }
}
