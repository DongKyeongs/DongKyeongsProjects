package com.dkp.exchange.dto;

import com.dkp.exchange.model.User;
import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private User user;

    public AuthResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }
} 