package com.example.virtual_exchange.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StockServiceTest {

    @Autowired
    StockService stockService;

    @Test
    void getPriceTest() throws InterruptedException {
        stockService.getStockPrice();

        // 비동기 통신이라 결과 올 때까지 3초만 기다려줌 (테스트라서 필요)
        Thread.sleep(3000);
    }
}