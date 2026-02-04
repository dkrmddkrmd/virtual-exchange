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

    // 1초마다 실행 (이전 작업 끝나고 1초 뒤)
    @Scheduled(fixedDelay = 1000)
    public void updateStockPrices() {
        // 1. 데이터 가져오기 (트랜잭션 X, 가벼움)
        List<UpbitTickerDto> tickerList = stockService.fetchUpbitData();

        // 2. 데이터가 있으면 DB에 넣기 (트랜잭션 O, 무거움)
        // ★ 외부에서 호출하므로 @Transactional이 정상 작동함!
        if (!tickerList.isEmpty()) {
            stockService.bulkUpdateStocks(tickerList);
        }
    }
}