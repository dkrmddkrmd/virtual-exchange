package com.example.virtual_exchange.scheduler;

import com.example.virtual_exchange.exception.UpbitApiCallException;
import com.example.virtual_exchange.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StockSchedulerTest {
    @Mock
    StockService stockService;
    @InjectMocks
    StockScheduler scheduler;

    @Test
    @DisplayName("업비트 조회 실패시에도 스케줄러는 진행")
    public void 스케줄러_테스트(){
        //Given
        Mockito.when(stockService.fetchUpbitData()).thenThrow(new UpbitApiCallException("업비트 서버 오류 발생"));

        //When
        scheduler.updateStockPrices();

        //Then
        Mockito.verify(stockService, Mockito.times(1)).fetchUpbitData();
        Mockito.verify(stockService, Mockito.never()).bulkUpdateStocks(Mockito.any());
    }
}
