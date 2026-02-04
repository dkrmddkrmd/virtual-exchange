package com.example.virtual_exchange.config.applicationRunner;

import com.example.virtual_exchange.dto.UpbitTickerDto;
import com.example.virtual_exchange.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockDataInitializer implements ApplicationRunner {

    private final StockService stockService;

    // 서버 실행 시 딱 한 번 자동으로 실행됨
    @Override
    public void run(ApplicationArguments args) {
        log.info("🚀 [초기화] 주식 데이터 초기 적재 시작...");

        List<UpbitTickerDto> tickers = stockService.fetchUpbitData();
        if (!tickers.isEmpty()) {
            // 외부 호출이므로 @Transactional 완벽 적용됨! 경고 사라짐!
            stockService.bulkUpdateStocks(tickers);
        }

        log.info("✅ [초기화] 주식 데이터 적재 완료");
    }
}