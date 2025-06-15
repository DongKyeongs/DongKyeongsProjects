package com.dkp.exchange.service;

import com.dkp.exchange.model.ChartData;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ChartService {

    private final Random random = new Random();

    public List<ChartData> getChartData(String pair, String interval, int limit) {
        List<ChartData> data = new ArrayList<>();
        BigDecimal basePrice = getBasePrice(pair);
        LocalDateTime currentTime = LocalDateTime.now();

        for (int i = 0; i < limit; i++) {
            LocalDateTime time = currentTime.minusMinutes(i * getIntervalMinutes(interval));
            BigDecimal open = basePrice.add(getRandomPriceChange());
            BigDecimal high = open.add(getRandomPriceChange().abs());
            BigDecimal low = open.subtract(getRandomPriceChange().abs());
            BigDecimal close = high.add(low).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal volume = getRandomVolume();

            data.add(new ChartData(time, open, high, low, close, volume));
            basePrice = close;
        }

        return data;
    }

    private BigDecimal getBasePrice(String pair) {
        return switch (pair) {
            case "BTC/USDT" -> new BigDecimal("50000");
            case "ETH/USDT" -> new BigDecimal("3000");
            default -> new BigDecimal("100");
        };
    }

    private int getIntervalMinutes(String interval) {
        return switch (interval) {
            case "1m" -> 1;
            case "5m" -> 5;
            case "15m" -> 15;
            case "30m" -> 30;
            case "1h" -> 60;
            case "4h" -> 240;
            case "1d" -> 1440;
            default -> 60;
        };
    }

    private BigDecimal getRandomPriceChange() {
        return new BigDecimal(random.nextDouble() * 100 - 50)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getRandomVolume() {
        return new BigDecimal(random.nextDouble() * 1000)
                .setScale(2, RoundingMode.HALF_UP);
    }
} 