package com.dkp.exchange.model;

import lombok.Data;

@Data
public class ExchangeSettings {
    private String userId;
    private String theme; // light, dark
    private String language; // ko, en
    private boolean priceAlertsEnabled;
    private boolean orderNotificationsEnabled;
    private boolean systemNotificationsEnabled;
    private double makerFee; // 메이커 수수료
    private double takerFee; // 테이커 수수료
    private double minTradeAmount; // 최소 거래 금액
    private double maxTradeAmount; // 최대 거래 금액
} 