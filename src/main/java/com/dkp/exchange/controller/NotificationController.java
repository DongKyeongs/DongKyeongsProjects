package com.dkp.exchange.controller;

import com.dkp.exchange.model.Notification;
import com.dkp.exchange.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PostMapping("/{userId}/price-alert")
    public ResponseEntity<Void> createPriceAlert(
            @PathVariable String userId,
            @RequestParam String pair,
            @RequestParam double targetPrice,
            @RequestParam boolean isAbove) {
        notificationService.createPriceAlert(userId, pair, targetPrice, isAbove);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String userId,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }
} 