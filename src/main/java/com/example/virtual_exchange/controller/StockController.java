package com.example.virtual_exchange.controller;

import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
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
    public List<Stock> getStocks(){
        return stockService.getStocks();
    }
}
