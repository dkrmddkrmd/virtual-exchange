package com.example.virtual_exchange.scheduler;

import com.example.virtual_exchange.dto.UpbitTickerDto;
import com.example.virtual_exchange.exception.UpbitApiCallException;
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

    // 이전 작업 끝나고 10초 뒤
    @Scheduled(fixedDelay = 10000)
    public void updateStockPrices() {
        try {
            List<UpbitTickerDto> tickerList = stockService.fetchUpbitData();

            if (!tickerList.isEmpty()) {
                stockService.bulkUpdateStocks(tickerList);
                log.info("업비트 시세 갱신 완료: {}개 종목 업데이트 됨", tickerList.size());
            }
        } catch (UpbitApiCallException e) {
            log.warn("이번 스케줄러 시세 갱신 실패, 10초 뒤 다시 시도합니다. 사유: {}", e.getMessage());
        }
    }
}