package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.StockHolding;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class OrderServiceTest {

    @Autowired OrderService orderService;

    // 리포지토리 5총사 (데이터 생성 및 삭제용)
    @Autowired OrderRepository orderRepository;
    @Autowired UserRepository userRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired StockRepository stockRepository;
    @Autowired StockHoldingRepository stockHoldingRepository;

    // ★ [추가된 핵심] 테스트 시작 전에 무조건 청소부터! (좀비 데이터 방지)
    @BeforeEach
    public void cleanup() {
        tearDown();
    }

    @AfterEach
    public void tearDown() {
        // [삭제 순서 중요] 외래키(FK)로 묶인 자식부터 지워야 에러가 안 남
        stockHoldingRepository.deleteAll(); // 1. 보유 주식 (User 참조 중)
        orderRepository.deleteAll();        // 2. 주문 내역 (User 참조 중)
        accountRepository.deleteAll();      // 3. 계좌 (User 참조 중)
        userRepository.deleteAll();         // 4. 유저 (이제 삭제 가능)
        stockRepository.deleteAll();        // 5. 종목
    }

    @Test
    @DisplayName("동시에 100개 주문이 들어오면 잔고가 망가진다 (실패해야 정상)")
    void concurrentOrderTest() throws InterruptedException {
        // 1. [준비] 데이터 세팅 (DB에 진짜 저장)

        // 1000원짜리 주식 생성
        Stock stock = new Stock("KRW-TEST", "테스트코인", 1000.0);
        stockRepository.save(stock);

        // 유저 생성
        User user = new User("test@test.com", "1234", "테스터");
        userRepository.save(user);

        // 계좌 생성 (잔액 1,000원 -> 딱 1개만 살 수 있음)
        Account account = new Account(user);
        account.increaseBalance(1000L);
        accountRepository.save(account);

        // 2. [공격] 100명이 동시에 '매수' 버튼을 누름!
        int threadCount = 100;
        // 32명의 일꾼을 가진 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        // 100명 다 끝날 때까지 기다리는 카운터
        CountDownLatch latch = new CountDownLatch(threadCount);

        OrderRequestDto req = new OrderRequestDto("KRW-TEST", 1L, "BUY");

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 따닥! 주문 요청
                    orderService.createOrder(user.getId(), req);
                } catch (Exception e) {
                    // 잔액 부족 등으로 실패하는 게 정상이므로 에러는 무시
                } finally {
                    // 성공하든 실패하든 카운트 1 감소
                    latch.countDown();
                }
            });
        }

        latch.await(); // 100명 다 끝날 때까지 대기

// 3. [검증] 결과 확인
        Account resultAccount = accountRepository.findByUserId(user.getId()).orElseThrow();
        long successfulOrders = orderRepository.count();

        // ★ [수정] 10개가 튀어나와도 당황하지 않고 다 가져와서 더해봅니다.
        List<StockHolding> holdings = stockHoldingRepository.findAllByUserIdAndStockCode(user.getId(), "KRW-TEST");

        // 리스트에 있는 모든 수량을 합침 (Java Stream 활용)
        long totalStockQuantity = holdings.stream()
                .mapToLong(StockHolding::getQuantity)
                .sum();

        System.out.println("=========================================");
        System.out.println("1. 시도한 횟수   : " + threadCount + "번");
        System.out.println("2. 성공한 주문 수 : " + successfulOrders + "개 (주문서 개수)");
        System.out.println("3. 보유 주식 총합 : " + totalStockQuantity + "개 (데이터 " + holdings.size() + "건의 합)");
        System.out.println("4. 최종 잔액     : " + resultAccount.getBalance() + "원");
        System.out.println("=========================================");

        // [핵심 검증] 주문 성공 횟수는 딱 1번이어야 함!
        assertThat(successfulOrders).isEqualTo(1L);
    }
}