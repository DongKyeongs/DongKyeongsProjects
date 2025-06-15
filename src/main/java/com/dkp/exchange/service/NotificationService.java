package com.dkp.exchange.service;

import com.dkp.exchange.model.Notification;
import com.dkp.exchange.model.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, List<Notification>> userNotifications = new ConcurrentHashMap<>();
    private final Map<String, Set<PriceAlert>> priceAlerts = new ConcurrentHashMap<>();

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(String userId, Notification notification) {
        userNotifications.computeIfAbsent(userId, k -> new ArrayList<>()).add(notification);
        messagingTemplate.convertAndSendToUser(
            userId,
            "/topic/notifications",
            notification
        );
    }

    public void createPriceAlert(String userId, String pair, double targetPrice, boolean isAbove) {
        priceAlerts.computeIfAbsent(userId, k -> new HashSet<>())
            .add(new PriceAlert(pair, targetPrice, isAbove));
    }

    public void checkPriceAlerts(String pair, double currentPrice) {
        priceAlerts.forEach((userId, alerts) -> {
            alerts.stream()
                .filter(alert -> alert.pair.equals(pair))
                .filter(alert -> (alert.isAbove && currentPrice >= alert.targetPrice) ||
                               (!alert.isAbove && currentPrice <= alert.targetPrice))
                .forEach(alert -> {
                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setType(Notification.NotificationType.PRICE_ALERT);
                    notification.setTitle("가격 알림");
                    notification.setMessage(String.format("%s 가격이 %s에 도달했습니다. (현재가: %.2f)",
                        pair, alert.targetPrice, currentPrice));
                    notification.setCreatedAt(LocalDateTime.now());
                    sendNotification(userId, notification);
                });
        });
    }

    public void notifyOrderFilled(Order order) {
        Notification notification = new Notification();
        notification.setUserId(order.getUserId());
        notification.setType(Notification.NotificationType.ORDER_FILLED);
        notification.setTitle("주문 체결");
        notification.setMessage(String.format("%s %s 주문이 체결되었습니다. (가격: %s, 수량: %s)",
            order.getPair(), order.getSide(), order.getPrice(), order.getFilledAmount()));
        notification.setCreatedAt(LocalDateTime.now());
        sendNotification(order.getUserId(), notification);
    }

    public List<Notification> getUserNotifications(String userId) {
        return userNotifications.getOrDefault(userId, new ArrayList<>());
    }

    public void markAsRead(String userId, Long notificationId) {
        userNotifications.getOrDefault(userId, new ArrayList<>())
            .stream()
            .filter(n -> n.getId().equals(notificationId))
            .findFirst()
            .ifPresent(n -> n.setRead(true));
    }

    private static class PriceAlert {
        final String pair;
        final double targetPrice;
        final boolean isAbove;

        PriceAlert(String pair, double targetPrice, boolean isAbove) {
            this.pair = pair;
            this.targetPrice = targetPrice;
            this.isAbove = isAbove;
        }
    }
} 