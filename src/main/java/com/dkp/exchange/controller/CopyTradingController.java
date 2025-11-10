package com.dkp.exchange.controller;

import com.dkp.exchange.model.*;
import com.dkp.exchange.service.CopyTradingService;
import com.dkp.exchange.service.SignalProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/copy-trading")
@RequiredArgsConstructor
public class CopyTradingController {
    private final SignalProviderService providerService;
    private final CopyTradingService copyTradingService;

    // ==================== Signal Provider ====================

    @GetMapping("/providers")
    public ResponseEntity<List<SignalProvider>> getProviders() {
        List<SignalProvider> providers = providerService.getActiveProviders();
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/providers/top")
    public ResponseEntity<List<SignalProvider>> getTopPerformers(
            @RequestParam(defaultValue = "10") int limit) {
        List<SignalProvider> providers = providerService.getTopPerformers(limit);
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/providers/popular")
    public ResponseEntity<List<SignalProvider>> getMostFollowed(
            @RequestParam(defaultValue = "10") int limit) {
        List<SignalProvider> providers = providerService.getMostFollowed(limit);
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/providers/{providerId}")
    public ResponseEntity<SignalProvider> getProviderDetails(@PathVariable Long providerId) {
        SignalProvider provider = providerService.getProviderDetails(providerId);
        return ResponseEntity.ok(provider);
    }

    @GetMapping("/providers/{providerId}/performance")
    public ResponseEntity<List<SignalProviderPerformance>> getProviderPerformance(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "30") int days) {
        List<SignalProviderPerformance> performance = providerService.getProviderPerformance(providerId, days);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/providers/search")
    public ResponseEntity<List<SignalProvider>> searchProviders(@RequestParam String keyword) {
        List<SignalProvider> providers = providerService.searchProviders(keyword);
        return ResponseEntity.ok(providers);
    }

    @PostMapping("/providers/register")
    public ResponseEntity<SignalProvider> registerAsProvider(
            @RequestParam Long userId,
            @RequestBody SignalProvider providerData) {
        SignalProvider provider = providerService.registerAsProvider(userId, providerData);
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/providers/{providerId}")
    public ResponseEntity<Map<String, String>> updateProviderSettings(
            @PathVariable Long providerId,
            @RequestBody SignalProvider updates) {
        providerService.updateProviderSettings(providerId, updates);
        return ResponseEntity.ok(Map.of("message", "Provider settings updated successfully"));
    }

    @PostMapping("/providers/{providerId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateProvider(@PathVariable Long providerId) {
        providerService.deactivateProvider(providerId);
        return ResponseEntity.ok(Map.of("message", "Provider deactivated successfully"));
    }

    // ==================== Copy Trade ====================

    @PostMapping("/start")
    public ResponseEntity<CopyTrade> startCopyTrade(
            @RequestParam Long followerId,
            @RequestParam Long providerId,
            @RequestBody CopyTrade settings) {
        CopyTrade copyTrade = copyTradingService.startCopyTrade(followerId, providerId, settings);
        return ResponseEntity.ok(copyTrade);
    }

    @PostMapping("/{copyTradeId}/stop")
    public ResponseEntity<Map<String, String>> stopCopyTrade(@PathVariable Long copyTradeId) {
        copyTradingService.stopCopyTrade(copyTradeId);
        return ResponseEntity.ok(Map.of("message", "Copy trade stopped successfully"));
    }

    @GetMapping("/my-copies")
    public ResponseEntity<List<CopyTrade>> getMyCopies(@RequestParam Long followerId) {
        List<CopyTrade> copyTrades = copyTradingService.getFollowerCopyTrades(followerId);
        return ResponseEntity.ok(copyTrades);
    }

    @GetMapping("/{copyTradeId}/executions")
    public ResponseEntity<List<CopyTradeExecution>> getCopyTradeExecutions(@PathVariable Long copyTradeId) {
        List<CopyTradeExecution> executions = copyTradingService.getCopyTradeExecutions(copyTradeId);
        return ResponseEntity.ok(executions);
    }
}
