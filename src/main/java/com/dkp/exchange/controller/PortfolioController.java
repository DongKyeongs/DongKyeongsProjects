package com.dkp.exchange.controller;

import com.dkp.exchange.model.Portfolio;
import com.dkp.exchange.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<Portfolio>> getUserPortfolios(@PathVariable String userId) {
        return ResponseEntity.ok(portfolioService.getUserPortfolios(userId));
    }

    @GetMapping("/{userId}/{asset}")
    public ResponseEntity<Portfolio> getPortfolio(
            @PathVariable String userId,
            @PathVariable String asset) {
        return ResponseEntity.ok(portfolioService.getPortfolio(userId, asset));
    }
} 