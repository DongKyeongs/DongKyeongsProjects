package com.dkp.exchange.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends ExchangeException {
    public UserAlreadyExistsException(String username) {
        super("User already exists: " + username, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
    }
}
