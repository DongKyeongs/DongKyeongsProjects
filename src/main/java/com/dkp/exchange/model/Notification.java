package com.dkp.exchange.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;
    private String userId;
    private NotificationType type;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public enum NotificationType {
        PRICE_ALERT,    // 가격 알림
        ORDER_FILLED,   // 주문 체결
        ORDER_CANCELLED,// 주문 취소
        SYSTEM_ALERT    // 시스템 알림
    }
} 