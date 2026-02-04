package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.dto.StockResponseDto;
import com.example.virtual_exchange.dto.UpbitTickerDto;
import com.example.virtual_exchange.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    private static final String MARKET_CODES =
            "KRW-BTC,KRW-XAUT,KRW-ETH,KRW-BCH,KRW-AAVE," +
                    "KRW-SOL,KRW-COMP,KRW-BSV,KRW-LINK,KRW-AVAX," +
                    "KRW-ETC,KRW-ENS,KRW-EGLD,KRW-AUCTION,KRW-UNI," +
                    "KRW-TRUMP,KRW-INJ,KRW-NEO,KRW-FLUID,KRW-LPT," +
                    "KRW-IP,KRW-ATOM,KRW-VANA,KRW-RENDER,KRW-DOT";

    /**
     * 1. 외부 API 호출 (Transaction 없음 - 가벼움)
     * - 단순히 데이터를 가져와서 리턴만 합니다.
     */
    public List<UpbitTickerDto> fetchUpbitData() {
        try {
            return WebClient.create("https://api.upbit.com")
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/ticker")
                            .queryParam("markets", MARKET_CODES)
                            .build())
                    .retrieve()
                    .bodyToFlux(UpbitTickerDto.class)
                    .collectList()
                    .block(); // 동기 처리
        } catch (Exception e) {
            log.error("[Upbit API Error] 시세 조회 실패", e);
            return Collections.emptyList(); // 에러 나면 빈 리스트 반환
        }
    }

    /**
     * 2. DB 일괄 업데이트 (Transaction 필수)
     * - saveAll()을 사용하여 쿼리 횟수를 획기적으로 줄입니다.
     */
    @Transactional
    public void bulkUpdateStocks(List<UpbitTickerDto> tickerList) {
        if (tickerList == null || tickerList.isEmpty()) return;

        // [최적화 로직]
        // 1. DB에 있는 기존 주식들을 한 번에 다 가져옵니다. (SELECT 1번)
        // findAllById를 쓰기 위해 ID(마켓코드)만 추출
        List<String> marketCodes = tickerList.stream()
                .map(UpbitTickerDto::getMarket)
                .toList();

        List<Stock> existingStocks = stockRepository.findAllById(marketCodes);

        // 2. 검색 속도를 위해 Map으로 변환 (Key: 코드, Value: Stock엔티티)
        Map<String, Stock> stockMap = existingStocks.stream()
                .collect(Collectors.toMap(Stock::getCode, stock -> stock));

        List<Stock> stocksToSave = new ArrayList<>();

        // 3. API 데이터 루프 돌면서 업데이트 or 신규생성
        for (UpbitTickerDto dto : tickerList) {
            Stock stock = stockMap.get(dto.getMarket());

            if (stock != null) {
                // 이미 있으면 가격만 업데이트 (Dirty Checking이 되지만, saveAll에 넣어도 무관)
                stock.updatePrice(dto.getTrade_price());
                stocksToSave.add(stock);
            } else {
                // 없으면 새로 생성
                Stock newStock = new Stock(dto.getMarket(), dto.getMarket(), dto.getTrade_price());
                stocksToSave.add(newStock);
            }
        }

        // 4. 한 방에 저장 (INSERT + UPDATE 쿼리 최적화)
        stockRepository.saveAll(stocksToSave);
    }

    // 조회: Entity -> DTO 변환해서 반환
    @Transactional(readOnly = true)
    @Cacheable(value = "stocks", key = "'allStocks'")
    public List<StockResponseDto> getAllStocks() {
        // 캐시 없을 때만 로그 찍힘
        log.info("[Cache Miss] DB에서 주식 목록 조회");

        return stockRepository.findAll().stream()
                .map(StockResponseDto::new)
                .toList();
    }
}