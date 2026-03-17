package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.StockHolding;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.repository.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class OrderTransactionTest {
    @Mock
    UserRepository userRepository;
    @Mock
    StockRepository stockRepository;
    @Mock
    AccountRepository accountRepository;
    @Mock
    StockHoldingRepository stockHoldingRepository;
    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    OrderTransactionService orderTransactionService;

    @Test
    @DisplayName("매수가 잘 되는지 확인")
    public void 매수_테스트(){
        //Given
        User user = new User("test@test.com", "1234", "tester");
        Stock stock = new Stock("Coin", "coin", 1000D);
        Account account = new Account(user);
        account.increaseBalance(10000L);

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(stockRepository.findById(Mockito.any())).thenReturn(Optional.of(stock));
        Mockito.when(accountRepository.findByUserId(Mockito.any())).thenReturn(Optional.of(account));
        // 처음 사는 주식이니까 보유 주식은 없다고 설정
        Mockito.when(stockHoldingRepository.findByUserAndStock(user, stock)).thenReturn(Optional.empty());

        //When
        orderTransactionService.buy(user.getId(), stock.getCode(), 1L);

        //Then
        Mockito.verify(stockHoldingRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(orderRepository, Mockito.times(1)).save(Mockito.any());
        Assertions.assertEquals(9000L, account.getBalance());
    }

    @Test
    @DisplayName("매도할 주식이 없을 때")
    public void 매도_테스트(){
        //Given
        User user = new User("test@test.com", "1234", "tester");
        Stock stock = new Stock("Coin", "coin", 1000D);
        Account account = new Account(user);
        account.increaseBalance(10000L);

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(stockRepository.findById(Mockito.any())).thenReturn(Optional.of(stock));
        Mockito.when(accountRepository.findByUserId(Mockito.any())).thenReturn(Optional.of(account));
        // 처음 사는 주식이니까 보유 주식은 없다고 설정
        Mockito.when(stockHoldingRepository.findByUserAndStock(user, stock)).thenReturn(Optional.empty());

        //When & Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            orderTransactionService.sell(user.getId(), stock.getCode(), 1L);
        });
    }

    @Test
    @DisplayName("매도가 잘 되는지")
    public void 매도_성공_테스트(){
        //Given
        User user = new User("test@test.com", "1234", "tester");
        Stock stock = new Stock("Coin", "coin", 1000D);
        Account account = new Account(user);
        account.increaseBalance(10000L);
        StockHolding stockHolding = new StockHolding(user, stock, 1L, 2000D);

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(stockRepository.findById(Mockito.any())).thenReturn(Optional.of(stock));
        Mockito.when(accountRepository.findByUserId(Mockito.any())).thenReturn(Optional.of(account));
        // 처음 사는 주식이니까 보유 주식은 없다고 설정
        Mockito.when(stockHoldingRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(stockHolding));

        //When
        orderTransactionService.sell(user.getId(), stock.getCode(), 1L);

        //Then
        Mockito.verify(orderRepository, Mockito.times(1)).save(Mockito.any());
        Assertions.assertEquals(11000L, account.getBalance());
        Assertions.assertEquals(0L, stockHolding.getQuantity());

    }

    @Test
    @DisplayName("잔고가 부족할 때는 매수가 안되도록 합니다.")
    public void 잔고_부족_테스트(){
        //Given
        User user = new User("test@test.com", "1234", "tester");
        Stock stock = new Stock("Coin", "coin", 10000D);
        Account account = new Account(user);
        account.increaseBalance(1000L);

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(stockRepository.findById(Mockito.any())).thenReturn(Optional.of(stock));
        Mockito.when(accountRepository.findByUserId(Mockito.any())).thenReturn(Optional.of(account));

        //When & Then
        Assertions.assertThrows(IllegalStateException.class, () -> {
           orderTransactionService.buy(user.getId(), stock.getCode(), 1L);
        });
    }

    @Test
    @DisplayName("추가 매수 확인 테스트")
    public void 추가_매수_성공_테스트(){
        //Given
        User user = new User("test@test.com", "1234", "tester");
        Stock stock = new Stock("Coin", "coin", 4000D);
        Account account = new Account(user);
        account.increaseBalance(10000L);
        StockHolding stockHolding = new StockHolding(user, stock, 2L, 1000D);

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(stockRepository.findById(Mockito.any())).thenReturn(Optional.of(stock));
        Mockito.when(accountRepository.findByUserId(Mockito.any())).thenReturn(Optional.of(account));
        // 처음 사는 주식이니까 보유 주식은 없다고 설정
        Mockito.when(stockHoldingRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(stockHolding));

        //When
        orderTransactionService.buy(user.getId(), stock.getCode(), 1L);

        //Then
        Mockito.verify(orderRepository, Mockito.times(1)).save(Mockito.any());
        Assertions.assertEquals(6000L, account.getBalance());
        Assertions.assertEquals(3L, stockHolding.getQuantity());
        Assertions.assertEquals(2000D, stockHolding.getAvgPrice());
    }

    @Test
    @DisplayName("보유 주식 개수보다 많은 매도 시도 시 에러가 잘 뜨는지")
    public void 초과_매도_실패_테스트(){
        //Given
        User user = new User("test@test.com", "1234", "tester");
        Stock stock = new Stock("Coin", "coin", 1000D);
        Account account = new Account(user);
        account.increaseBalance(10000L);
        StockHolding stockHolding = new StockHolding(user, stock, 1L, 2000D);

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(stockRepository.findById(Mockito.any())).thenReturn(Optional.of(stock));
        Mockito.when(accountRepository.findByUserId(Mockito.any())).thenReturn(Optional.of(account));
        // 처음 사는 주식이니까 보유 주식은 없다고 설정
        Mockito.when(stockHoldingRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(stockHolding));

        //When & Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
           orderTransactionService.sell(user.getId(), stock.getCode(), 3L);
        });
    }
}
