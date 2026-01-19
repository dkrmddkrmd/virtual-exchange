package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.dto.UpbitTickerDto;
import com.example.virtual_exchange.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@ Slf4j
@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    private static final String MARKETS =
            "KRW-BTC,KRW-XAUT,KRW-ETH,KRW-BCH,KRW-AAVE," + // 비트, 테더골드, 이더, 비캐, 에이브
                    "KRW-SOL,KRW-COMP,KRW-BSV,KRW-LINK,KRW-AVAX," + // 솔라나, 컴파운드, BSV, 체인링크, 아발란체
                    "KRW-ETC,KRW-ENS,KRW-EGLD,KRW-AUCTION,KRW-UNI," + // 이클, ENS, 멀티버스, 바운스, 유니
                    "KRW-TRUMP,KRW-INJ,KRW-NEO,KRW-FLUID,KRW-LPT," + // 트럼프, 인젝티브, 네오, 플루이드, 라이브피어
                    "KRW-IP,KRW-ATOM,KRW-VANA,KRW-RENDER,KRW-DOT";

    //서버 시작 시 1회 실행 (내부 호출이지만 Repository가 있어서 안전함)
    @PostConstruct
    public void init() {
        getStockPrice();
    }

    @Transactional
    public void getStockPrice() {
        // 1. [수정] 안전한 메이저 코인 30개 리스트 (상장폐지 위험 적은 종목 위주)
        // 공백 없이 콤마(,)로만 연결해야 합니다.
        String markets = MARKETS;

        WebClient.create("https://api.upbit.com") // 기본 URL 설정
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/ticker") // 경로 설정
                        .queryParam("markets", markets) // [중요] 쿼리 파라미터 자동 인코딩
                        .build())
                .retrieve()
                .bodyToFlux(UpbitTickerDto.class)
                .subscribe(dto -> {
                    // 로그 확인
                    // System.out.println("종목: " + dto.getMarket() + ", 가격: " + dto.getTrade_price());

                    Stock stock = stockRepository.findById(dto.getMarket())
                            .orElse(null);

                    if (stock == null) {
                        // 업비트는 한글 이름을 안 주므로, 일단 코드로 이름 저장 (나중에 매핑 가능)
                        Stock newStock = new Stock(dto.getMarket(), dto.getMarket(), dto.getTrade_price());
                        stockRepository.save(newStock);
                    } else {
                        stock.updatePrice(dto.getTrade_price());
                        // Dirty Checking으로 자동 저장됨
                    }
                }, error -> {
                    // [에러 처리] 로그를 찍어서 원인을 파악합니다.
                    System.err.println("업비트 API 호출 중 에러 발생: " + error.getMessage());
                });
    }

    /**
     * 전체 주식 목록 조회
     * - @Cacheable: 이 메서드의 결과를 캐시(Redis)에 저장해라!
     * - value = "stocks": 캐시 저장소 이름표 (서랍장 이름)
     * - key = "'allStocks'": 데이터 식별키 (서랍장 칸 이름)
     * * 동작 원리:
     * 1. 요청이 오면 Redis에 'stocks::allStocks' 라는 키가 있는지 확인.
     * 2. 있으면? -> 메서드 실행 안 하고 Redis에 있는 거 바로 리턴. (DB 안 감!)
     * 3. 없으면? -> 메서드 실행해서 DB 조회하고, 그 결과를 Redis에 저장 후 리턴.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "stocks", key = "'allStocks'") // ★ 여기가 핵심입니다!
    public List<Stock> getStocks() {
        log.info("📢 [DB 조회] 캐시가 없어서 DB에서 주식 목록을 가져옵니다..."); // 로그로 확인해보세요
        return stockRepository.findAll();
    }
}