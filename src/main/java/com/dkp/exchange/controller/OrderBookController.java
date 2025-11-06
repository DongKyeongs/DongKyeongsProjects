package com.dkp.exchange.controller;

import com.dkp.exchange.service.RedisOrderBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orderbook")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class OrderBookController {
    private final RedisOrderBookService orderBookService;

    @GetMapping("/{symbol}")
    public ResponseEntity<RedisOrderBookService.OrderBookSnapshot> getOrderBook(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "20") int depth) {
        return ResponseEntity.ok(orderBookService.getOrderBookSnapshot(symbol, depth));
    }

    @GetMapping("/{symbol}/bids")
    public ResponseEntity<?> getBuyOrders(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(orderBookService.getBuyOrders(symbol, limit));
    }

    @GetMapping("/{symbol}/asks")
    public ResponseEntity<?> getSellOrders(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(orderBookService.getSellOrders(symbol, limit));
    }
}
