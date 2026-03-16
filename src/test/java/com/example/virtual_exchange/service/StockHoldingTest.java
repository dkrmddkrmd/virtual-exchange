package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.StockHolding;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.MyAssetDto;
import com.example.virtual_exchange.dto.StockHoldingDto;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.StockHoldingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class StockHoldingTest {
    @Mock
    StockHoldingRepository stockHoldingRepository;
    @Mock
    AccountRepository accountRepository;
    @InjectMocks
    StockHoldingService stockHoldingService;

    @Test
    @DisplayName("보유한 주식들의 가격이 변했을 때 잘 반영이 되어야 한다.")
    public void 보유주식_테스트(){
        //Given
        Stock stock = new Stock("test", "test", 2000D);
        User user = new User("test@test.com", "1234", "tester");
        Account account = new Account(user);
        account.increaseBalance(10000L);
        StockHolding stockHolding = new StockHolding(user, stock, 1L, 1000D);

        Mockito.when(accountRepository.findByUserId(user.getId())).thenReturn(Optional.of(account));
        Mockito.when(stockHoldingRepository.findAllByUserId(user.getId())).thenReturn(List.of(stockHolding));

        //When
        MyAssetDto myAssetDto = stockHoldingService.getMyAssetStatus(user.getId());

        //Then
        Assertions.assertEquals(1000L, myAssetDto.getTotalPurchaseAmount());
        Assertions.assertEquals(2000L, myAssetDto.getTotalEvaluationAmount());
        Assertions.assertEquals(1000L, myAssetDto.getTotalProfitLoss());
        Assertions.assertEquals(100L, myAssetDto.getReturnRate());
        Assertions.assertEquals(12000L, myAssetDto.getTotalAssetAmount());
    }

    @Test
    @DisplayName("유저 계좌가 없을 경우")
    public void 유저계좌_테스트(){
        //Given
        User user = new User("test@test.com", "1234", "tester");

        Mockito.when(accountRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        //When & Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            stockHoldingService.getMyAssetStatus(user.getId());
        });
    }

    @Test
    @DisplayName("주식이 0개인 경우")
    public void 주식_0개_테스트(){
        //Given
        User user = new User("test@test.com", "1234", "tester");
        Account account = new Account(user);
        account.increaseBalance(10000L);

        Mockito.when(accountRepository.findByUserId(user.getId())).thenReturn(Optional.of(account));
        Mockito.when(stockHoldingRepository.findAllByUserId(user.getId())).thenReturn(Collections.emptyList());

        //When
        MyAssetDto myAssetDto = stockHoldingService.getMyAssetStatus(user.getId());

        //Then
        Assertions.assertEquals(0D, myAssetDto.getReturnRate());
        Assertions.assertEquals(10000L, account.getBalance());
    }
}
