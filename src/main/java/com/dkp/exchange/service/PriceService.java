package com.dkp.exchange.service;

import com.dkp.exchange.dto.PriceResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PriceService {

    private final RestTemplate restTemplate;
    private static final String BINANCE_API_URL = "https://api.binance.com/api/v3/ticker/24hr";

    public PriceService() {
        this.restTemplate = new RestTemplate();
    }

    public List<PriceResponse> getPrices() {
        Object[] response = restTemplate.getForObject(BINANCE_API_URL, Object[].class);
        return Arrays.stream(response)
                .map(item -> {
                    if (item instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> map = (java.util.Map<String, Object>) item;
                        String symbol = (String) map.get("symbol");
                        if (symbol.endsWith("USDT")) {
                            return new PriceResponse(
                                symbol,
                                Double.parseDouble((String) map.get("lastPrice")),
                                Double.parseDouble((String) map.get("priceChangePercent")),
                                Double.parseDouble((String) map.get("volume"))
                            );
                        }
                    }
                    return null;
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    public PriceResponse getPrice(String symbol) {
        Object[] response = restTemplate.getForObject(BINANCE_API_URL, Object[].class);
        return Arrays.stream(response)
                .map(item -> {
                    if (item instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> map = (java.util.Map<String, Object>) item;
                        String responseSymbol = (String) map.get("symbol");
                        if (responseSymbol.equals(symbol)) {
                            return new PriceResponse(
                                responseSymbol,
                                Double.parseDouble((String) map.get("lastPrice")),
                                Double.parseDouble((String) map.get("priceChangePercent")),
                                Double.parseDouble((String) map.get("volume"))
                            );
                        }
                    }
                    return null;
                })
                .filter(item -> item != null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Price not found for symbol: " + symbol));
    }
} 