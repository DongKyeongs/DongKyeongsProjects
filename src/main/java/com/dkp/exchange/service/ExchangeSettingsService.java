package com.dkp.exchange.service;

import com.dkp.exchange.model.ExchangeSettings;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExchangeSettingsService {
    private final Map<String, ExchangeSettings> userSettings = new ConcurrentHashMap<>();

    public ExchangeSettings getSettings(String userId) {
        return userSettings.computeIfAbsent(userId, this::createDefaultSettings);
    }

    public ExchangeSettings updateSettings(String userId, ExchangeSettings settings) {
        settings.setUserId(userId);
        userSettings.put(userId, settings);
        return settings;
    }

    public double calculateFee(String userId, double amount, boolean isMaker) {
        ExchangeSettings settings = getSettings(userId);
        double feeRate = isMaker ? settings.getMakerFee() : settings.getTakerFee();
        return amount * feeRate;
    }

    public boolean validateTradeAmount(String userId, double amount) {
        ExchangeSettings settings = getSettings(userId);
        return amount >= settings.getMinTradeAmount() && amount <= settings.getMaxTradeAmount();
    }

    private ExchangeSettings createDefaultSettings(String userId) {
        ExchangeSettings settings = new ExchangeSettings();
        settings.setUserId(userId);
        settings.setTheme("light");
        settings.setLanguage("ko");
        settings.setPriceAlertsEnabled(true);
        settings.setOrderNotificationsEnabled(true);
        settings.setSystemNotificationsEnabled(true);
        settings.setMakerFee(0.001); // 0.1%
        settings.setTakerFee(0.002); // 0.2%
        settings.setMinTradeAmount(10.0); // 최소 10 USDT
        settings.setMaxTradeAmount(100000.0); // 최대 100,000 USDT
        return settings;
    }
} 