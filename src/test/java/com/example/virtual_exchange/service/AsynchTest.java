package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.StockHolding;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.facade.RedissonLockStockFacade;
import com.example.virtual_exchange.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach; // 추가됨
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AsynchTest {

    @Autowired private TestOrderService testOrderService;
    @Autowired private UserRepository userRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private StockHoldingRepository stockHoldingRepository;
    @Autowired private OrderRepository orderRepository;

    @BeforeEach
    public void setUp() {
        orderRepository.deleteAllInBatch();
        stockHoldingRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Phase 1 해결 검증: 분산 락 적용 시 100명 요청에도 딱 1명만 성공해야 함")
    public void concurrency_fix_test() throws InterruptedException {
        // [GIVEN] 데이터 세팅
        User user = userRepository.save(new User("tester@test.com", "1234", "tester"));
        Stock stock = stockRepository.save(new Stock("KRW-TEST", "테스트코인", 10000.0));

        // 잔액 10,000원 (딱 1개만 구매 가능)
        Account account = new Account(user);
        account.increaseBalance(10000L);
        accountRepository.save(account);

        // [WHEN] 100개 스레드 동시 요청
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // ★ [수정] DTO 생성하여 전달
                    OrderRequestDto requestDto = new OrderRequestDto("KRW-TEST", 1L, "BUY");

                    // Facade 호출 (분산 락 적용됨)
                    testOrderService.buy(user.getId(), requestDto);
                } catch (Exception e) {
                    // 실패(잔액 부족 등)는 자연스러운 현상
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // [THEN] 결과 확인
        long successfulOrders = orderRepository.count();
        List<StockHolding> holdings = stockHoldingRepository.findAllByUserId(user.getId());
        long totalStockQuantity = holdings.stream().mapToLong(StockHolding::getQuantity).sum();
        Account resultAccount = accountRepository.findByUserId(user.getId()).orElseThrow();

        System.out.println("========================================");
        System.out.println("1. 시도한 횟수      : " + threadCount + "번");
        System.out.println("2. 성공한 주문 수    : " + successfulOrders + "개 (기대값: 1)");
        System.out.println("3. 보유 주식 총합    : " + totalStockQuantity + "개");
        System.out.println("4. 최종 잔액        : " + resultAccount.getBalance() + "원");
        System.out.println("========================================");

        // ★ 검증: 분산 락 덕분에 딱 1개만 성공해야 함 (초록불!)
        assertThat(successfulOrders).isEqualTo(1L);
    }
}