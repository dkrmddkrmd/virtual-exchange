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

    // 10분마다 실행 (이전 작업 끝나고 10분 뒤 [600000]) - 1초는 1000
    @Scheduled(fixedDelay = 600000)
    public void updateStockPrices() {
        try {
            List<UpbitTickerDto> tickerList = stockService.fetchUpbitData();

            if (!tickerList.isEmpty()) {
                stockService.bulkUpdateStocks(tickerList);
                // 💡 작업이 '진짜로 성공' 했을 때, 맨 마지막에 몇 개나 갱신했는지 찍어주는 게 실무의 정석입니다.
                log.info("업비트 시세 갱신 완료: {}개 종목 업데이트 됨", tickerList.size());
            }
        } catch (UpbitApiCallException e) {
            // 💡 10분마다 도는 스케줄러가 예외 때문에 죽으면 안 되니까, 여기서 WARN만 찍고 다음 10분 뒤를 기약합니다.
            log.warn("이번 스케줄러 시세 갱신 실패, 10분 뒤 다시 시도합니다. 사유: {}", e.getMessage());
        }
    }
}