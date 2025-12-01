package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.dto.UpbitTickerDto;
import com.example.virtual_exchange.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    @Transactional
    public void getStockPrice() {
        // 업비트 API: 비트코인(BTC)과 이더리움(ETH) 가격을 달라고 요청
        String url = "https://api.upbit.com/v1/ticker?markets=KRW-BTC,KRW-ETH";

        WebClient.create() // WebClient 생성
                .get()     // GET 요청
                .uri(url)  // 주소 설정
                .retrieve() // 결과 주세요
                .bodyToFlux(UpbitTickerDto.class) // 받은 JSON을 DTO 리스트로 변환해라!
                .subscribe(dto -> {
                    // dto에는 "KRW-BTC", 70000000.0 같은 데이터가 들어있음
                    System.out.println("종목: " + dto.getMarket() + ", 가격: " + dto.getTrade_price());

                    // [미션] TODO: 여기서 리포지토리를 불러서 DB에 저장하거나 업데이트 하세요!
                    // 1. dto.getMarket() (코드)으로 DB에서 주식을 찾는다.
                    Stock stock = stockRepository.findById(dto.getMarket())
                            .orElse(null);
                    if (stock == null) {
                        // 2. 없으면? -> 새로 만들어서 저장 (INSERT)
                        // (업비트 Ticker API에는 한글 이름이 없어서, 일단 이름에도 코드를 넣습니다)
                        Stock newStock = new Stock(dto.getMarket(), dto.getMarket(), dto.getTrade_price());
                        stockRepository.save(newStock);
                    } else {
                        // 3. 있으면? -> 가격만 업데이트하고 저장 (UPDATE)
                        // (JPA의 Dirty Checking 기능으로 save 안 해도 되지만, 명시적으로 save 호출해도 무방)
                        stock.updatePrice(dto.getTrade_price());
                        stockRepository.save(stock);
                    }
                });
    }
}
