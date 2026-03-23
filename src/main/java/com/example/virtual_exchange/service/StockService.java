package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.dto.StockResponseDto;
import com.example.virtual_exchange.dto.UpbitTickerDto;
import com.example.virtual_exchange.exception.UpbitApiCallException;
import com.example.virtual_exchange.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
            throw new UpbitApiCallException("업비트 시세 조회 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    @CacheEvict(value = "stocks", key = "'allStocks'") // DB가 갱신될 때 기존 캐시를 폭파시킵니다!
    public void bulkUpdateStocks(List<UpbitTickerDto> tickerList) {
        // 입력 dto에서 market 값만 꺼냄
        List<String> marketCodes = tickerList.stream()
                .map(UpbitTickerDto::getMarket)
                .toList();

        // 내 repositroy에 존재하는 주식들
        List<Stock> existingStocks = stockRepository.findAllById(marketCodes);

        // 내 존재하는 주식들을 map형태로
        Map<String, Stock> stockMap = existingStocks.stream()
                .collect(Collectors.toMap(Stock::getCode, stock -> stock));

        List<Stock> stocksToSave = new ArrayList<>();

        // 입력받은 리스트들 dto
        for (UpbitTickerDto dto : tickerList) {
            Stock stock = stockMap.get(dto.getMarket());

            if (stock != null) {
                stock.updatePrice(dto.getTrade_price());
                stocksToSave.add(stock);
            } else {
                Stock newStock = new Stock(dto.getMarket(), dto.getMarket(), dto.getTrade_price());
                stocksToSave.add(newStock);
            }
        }

        stockRepository.saveAll(stocksToSave);
    }


    @Transactional(readOnly = true)
    @Cacheable(value = "stocks", key = "'allStocks'")
    public List<StockResponseDto> getAllStocks() {
        log.info("📢 [DB 조회] 캐시가 없어서 DB에서 주식 목록을 가져옵니다...");
        return stockRepository.findAll().stream()
                .map(StockResponseDto::new)
                .collect(Collectors.toList());
    }
}