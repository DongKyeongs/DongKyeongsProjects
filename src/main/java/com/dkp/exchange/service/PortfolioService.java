package com.dkp.exchange.service;

import com.dkp.exchange.model.Portfolio;
import com.dkp.exchange.model.Order;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class PortfolioService {
    private final Map<String, Portfolio> portfolios = new HashMap<>();

    public Portfolio getPortfolio(String userId, String asset) {
        String key = userId + ":" + asset;
        return portfolios.computeIfAbsent(key, k -> createNewPortfolio(userId, asset));
    }

    public List<Portfolio> getUserPortfolios(String userId) {
        return portfolios.values().stream()
                .filter(p -> p.getUserId().equals(userId))
                .toList();
    }

    public void updatePortfolio(Order order) {
        Portfolio portfolio = getPortfolio(order.getUserId(), order.getPair().split("/")[0]);
        
        if (order.getSide() == Order.OrderSide.BUY) {
            BigDecimal totalCost = order.getPrice().multiply(order.getFilledAmount());
            BigDecimal newBalance = portfolio.getBalance().add(order.getFilledAmount());
            BigDecimal newTotalInvestment = portfolio.getTotalInvestment().add(totalCost);
            
            portfolio.setBalance(newBalance);
            portfolio.setTotalInvestment(newTotalInvestment);
            portfolio.setAverageBuyPrice(newTotalInvestment.divide(newBalance, 8, BigDecimal.ROUND_HALF_UP));
        } else {
            portfolio.setBalance(portfolio.getBalance().subtract(order.getFilledAmount()));
        }
        
        portfolio.setLastUpdated(new java.util.Date());
    }

    private Portfolio createNewPortfolio(String userId, String asset) {
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(userId);
        portfolio.setAsset(asset);
        portfolio.setBalance(BigDecimal.ZERO);
        portfolio.setLockedBalance(BigDecimal.ZERO);
        portfolio.setAverageBuyPrice(BigDecimal.ZERO);
        portfolio.setTotalInvestment(BigDecimal.ZERO);
        portfolio.setLastUpdated(new java.util.Date());
        return portfolio;
    }
} 