package com.dkp.exchange.controller;

import com.dkp.exchange.model.ChartData;
import com.dkp.exchange.service.ChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chart")
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;

    @GetMapping("/{pair}")
    public ResponseEntity<List<ChartData>> getChartData(
            @PathVariable String pair,
            @RequestParam(defaultValue = "1h") String interval,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(chartService.getChartData(pair, interval, limit));
    }
} 