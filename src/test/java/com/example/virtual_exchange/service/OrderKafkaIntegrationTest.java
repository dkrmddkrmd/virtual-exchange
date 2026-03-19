package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.StockHoldingRepository;
import com.example.virtual_exchange.repository.StockRepository;
import com.example.virtual_exchange.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

@SpringBootTest
@EmbeddedKafka(partitions = 3, topics = {"stock_order"})
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class OrderKafkaIntegrationTest {
    @Autowired
    OrderService orderService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StockRepository stockRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    StockHoldingRepository stockHoldingRepository;

    @Test
    @DisplayName("진짜 카프카를 타고 매수 로직이 처음부터 끝까지 잘 도는지 확인한다.")
    public void 카프카_매수_통합_테스트() throws InterruptedException {
        //Given
        String randomString = UUID.randomUUID().toString().substring(0, 8);
        String email = "test_" + randomString + "@test.com";

        User user = new User(email, "1234", "tester");
        User findUser = userRepository.save(user);

        Account account = new Account(user);
        account.increaseBalance(10000L);
        accountRepository.save(account);

        Stock stock = new Stock("coin", "testCoin", 1000D);
        stockRepository.save(stock);

        OrderRequestDto dto = new OrderRequestDto(stock.getCode(), 1L, "BUY");

        //When
        orderService.createOrder(findUser.getId(), dto);

        //카프카는 비동기라서
        Thread.sleep(2000);

        //Then
        Account updatedAccount = accountRepository.findByUserId(user.getId()).orElseThrow();
        Assertions.assertEquals(9000L, updatedAccount.getBalance());
    }
}
