package com.dkp.exchange.controller;

import com.dkp.exchange.model.ExchangeSettings;
import com.dkp.exchange.service.ExchangeSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class ExchangeSettingsController {

    private final ExchangeSettingsService settingsService;

    @GetMapping("/{userId}")
    public ResponseEntity<ExchangeSettings> getSettings(@PathVariable String userId) {
        return ResponseEntity.ok(settingsService.getSettings(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ExchangeSettings> updateSettings(
            @PathVariable String userId,
            @RequestBody ExchangeSettings settings) {
        return ResponseEntity.ok(settingsService.updateSettings(userId, settings));
    }

    @GetMapping("/{userId}/fee")
    public ResponseEntity<Double> calculateFee(
            @PathVariable String userId,
            @RequestParam double amount,
            @RequestParam boolean isMaker) {
        return ResponseEntity.ok(settingsService.calculateFee(userId, amount, isMaker));
    }
} 