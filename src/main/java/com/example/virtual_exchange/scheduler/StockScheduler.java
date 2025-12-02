package com.example.virtual_exchange.scheduler;

import com.example.virtual_exchange.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockScheduler {
    private final StockService stockService;

    @Scheduled(cron = "0 * * * * *")
    public void updateStockPrices(){
        System.out.println("1분마다 코인 시세 갱신");
        stockService.getStockPrice();
    }
}
