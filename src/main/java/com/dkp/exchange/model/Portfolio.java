package com.dkp.exchange.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Portfolio {
    private Long id;
    private String userId;
    private String asset;
    private BigDecimal balance;
    private BigDecimal lockedBalance;
    private BigDecimal averageBuyPrice;
    private BigDecimal totalInvestment;
    private LocalDateTime lastUpdated;

    public BigDecimal getTotalBalance() {
        return balance.add(lockedBalance);
    }

    public BigDecimal getUnrealizedPnL(BigDecimal currentPrice) {
        return balance.multiply(currentPrice.subtract(averageBuyPrice));
    }

    public BigDecimal getUnrealizedPnLPercentage(BigDecimal currentPrice) {
        if (averageBuyPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentPrice.subtract(averageBuyPrice)
                .divide(averageBuyPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
} 