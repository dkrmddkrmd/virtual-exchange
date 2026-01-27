package com.example.virtual_exchange.scheduler;

import com.example.virtual_exchange.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockScheduler {
    private final StockService stockService;

    // [수정] cron (1분 1회) -> fixedDelay (1초 1회)
    // 1000ms = 1초마다 실행
    @Scheduled(fixedDelay = 100000)
    public void updateStockPrices(){
        // 로그가 너무 많이 찍히면 정신 없으니 주석 처리하거나 필요할 때만 켜세요
        // System.out.println("1초마다 코인 시세 갱신 중...");
        stockService.getStockPrice();
    }
}
