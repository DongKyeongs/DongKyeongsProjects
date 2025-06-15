package com.dkp.exchange.controller;

import com.dkp.exchange.dto.PriceResponse;
import com.dkp.exchange.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "http://localhost:3000")
public class PriceController {

    private final PriceService priceService;

    @Autowired
    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping
    public List<PriceResponse> getPrices() {
        return priceService.getPrices();
    }

    @GetMapping("/{symbol}")
    public PriceResponse getPrice(@PathVariable String symbol) {
        return priceService.getPrice(symbol);
    }
} 