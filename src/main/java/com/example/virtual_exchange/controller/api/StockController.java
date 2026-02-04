package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.dto.StockResponseDto;
import com.example.virtual_exchange.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {
    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<StockResponseDto>> getStocks() {
        List<StockResponseDto> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }
}
