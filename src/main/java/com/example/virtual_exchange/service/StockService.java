package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.dto.UpbitTickerDto;
import com.example.virtual_exchange.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    //서버 시작 시 1회 실행 (내부 호출이지만 Repository가 있어서 안전함)
    @PostConstruct
    public void init() {
        getStockPrice();
    }

    @Transactional
    public void getStockPrice() {
        // 1. [수정] 안전한 메이저 코인 30개 리스트 (상장폐지 위험 적은 종목 위주)
        // 공백 없이 콤마(,)로만 연결해야 합니다.
        String markets = "KRW-XRP,KRW-BTC,KRW-BARD,KRW-ETH,KRW-USDT,KRW-IP,KRW-MOVE,KRW-SOL,KRW-GRS,KRW-BREV,KRW-KAITO,KRW-DOGE,KRW-ZBT,KRW-SAFE,KRW-BERA,KRW-BCH,KRW-BOUNTY,KRW-ADA,KRW-AVNT,KRW-SUI,KRW-MED,KRW-CHZ,KRW-VIRTUAL,KRW-ME,KRW-AXS,KRW-ZKP,KRW-ONDO,KRW-XPL,KRW-XTZ,KRW-PENGU";

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

    public List<Stock> getStocks() {
        return stockRepository.findAll();
    }
}