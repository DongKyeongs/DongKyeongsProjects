package com.dkp.exchange.model;

public enum OrderStatus {
    PENDING,    // 주문 대기
    PARTIAL,    // 부분 체결
    COMPLETED,  // 완전 체결
    CANCELLED   // 취소됨
} 