package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.dto.UpbitTickerDto;
import com.example.virtual_exchange.repository.StockRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {
    private static final Logger log = LoggerFactory.getLogger(StockServiceTest.class);
    @Mock
    StockRepository stockRepository;

    @InjectMocks
    StockService stockService;

    @Captor
    ArgumentCaptor<List<Stock>> captor;

    @Test
    @DisplayName("기존 주식은 가격이 업데이트되고, 신규 주식은 새로 생성되어 저장된다.")
    public void 주식_업데이트_테스트(){
        //Given
        Stock stock1 = new Stock("test", "test", 1000D); // DB에 원래 있는 코인
        Stock stock2 = new Stock("testTwo", "testTwo", 2000D); // DB에 새로 넣을 코인

        Mockito.when(stockRepository.findAllById(List.of(stock1.getCode(), stock2.getCode())))
                .thenReturn(List.of(stock1));

        //When
        List<UpbitTickerDto> list = new ArrayList<>();
        // 바뀐 값
        list.add(new UpbitTickerDto(stock1.getCode(), 2000D));
        // 새로운 주식
        list.add(new UpbitTickerDto(stock2.getCode(), stock2.getCurrentPrice()));
        stockService.bulkUpdateStocks(list);

        //Then
        Mockito.verify(stockRepository, Mockito.times(1)).saveAll(captor.capture());

        List<Stock> ansList = captor.getValue();

        Assertions.assertEquals(2, ansList.size());
        Stock findStock1 = ansList.get(0);
        Stock findStock2 = ansList.get(1);
        Assertions.assertEquals(2000D, findStock1.getCurrentPrice());
        Assertions.assertEquals("testTwo", findStock2.getCode());
    }
}
