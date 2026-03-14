package com.example.virtual_exchange.scheduler;

import com.example.virtual_exchange.dto.UpbitTickerDto;
import com.example.virtual_exchange.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockScheduler {

    private final StockService stockService;

    // 10분마다 실행 (이전 작업 끝나고 10분 뒤 [600000]) - 1초는 1000
    @Scheduled(fixedDelay = 600000)
    public void updateStockPrices() {
        List<UpbitTickerDto> tickerList = stockService.fetchUpbitData();

        if (!tickerList.isEmpty()) {
            stockService.bulkUpdateStocks(tickerList);
        }
    }
}