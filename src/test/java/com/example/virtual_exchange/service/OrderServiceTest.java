package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.StockHolding;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired
    OrderService orderService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    StockRepository stockRepository;
    @Autowired
    StockHoldingRepository stockHoldingRepository;
    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("첫 매수: 잔고가 줄고, 보유 주식이 새로 생겨야 한다")
    void buyBewStockTest(){
        // [Given]: 테스트를 위한 "준비" 단계
        // 1. 유저, 계좌(잔고 1000원), 주식(100원)을 미리 만들어둔다.
        User user = userRepository.save(new User("poor@test.com", "1234", "거지"));

        Account account = new Account(user);
        account.increaseBalance(1000L); // 돈 충전은 준비 단계!
        accountRepository.save(account);

        Stock stock = stockRepository.save(new Stock("KRW-BTC", "비트코인", 100.0));

        // [When]: 실제로 테스트하고 싶은 "행동" (딱 한 줄인 경우가 많음)
        // 1. 1주를 매수한다.
        orderService.buy(user.getId(), stock.getCode(), 1L);

        // [Then]: 결과가 맞는지 "검증" 단계
        // 1. 잔고 확인 (중요: DB에서 다시 꺼내와야 함!)
        Account savedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(savedAccount.getBalance()).isEqualTo(900L); // 1000 - 100 = 900

        // 2. 주식이 생겼는지 확인 (이제는 조회가 됨!)
        StockHolding stockHolding = stockHoldingRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new IllegalArgumentException("주식이 안 사졌어요!"));

        assertThat(stockHolding.getStock().getCode()).isEqualTo("KRW-BTC");
        assertThat(stockHolding.getUser().getEmail()).isEqualTo("poor@test.com");
        assertThat(stockHolding.getQuantity()).isEqualTo(1L); // 수량 확인도 추가하면 좋음
    }

    @Test
    @DisplayName("추가 매수(물타기): 수량이 늘고 평단가가 갱신되어야 한다")
    void buyAdditionalStockTest() {
        // Given
        User user = userRepository.save(new User("test@test.com", "1234", "test"));
        Account account = accountRepository.save(new Account(user));
        account.increaseBalance(1000L); // 초기 잔액 1000원

        Stock stock = stockRepository.save(new Stock("KRW-BTC", "coin", 100.0));

        // 1차 매수 (100원에 1개)
        orderService.buy(user.getId(), stock.getCode(), 1L);

        // When
        // 가격이 200원으로 오름
        stock.updatePrice(200.0);
        stockRepository.save(stock); // (중요) 바뀐 가격을 DB에 확정!

        // 2차 매수 (200원에 2개)
        orderService.buy(user.getId(), stock.getCode(), 2L);

        // Then
        StockHolding stockHolding = stockHoldingRepository.findByUserAndStock(user, stock).get();

        // 1. 수량 검증 (1 + 2 = 3) -> OK 하셨음
        assertThat(stockHolding.getQuantity()).isEqualTo(3);

        // 2. 잔고 검증 (1000 - 100 - 400 = 500) -> OK 하셨음
        Account findAccount = accountRepository.findByUserId(user.getId()).get();
        assertThat(findAccount.getBalance()).isEqualTo(500L);

        // 3. [추가] 평단가 검증 (핵심!)
        // 계산: (100*1 + 200*2) / 3 = 166.6666...
        // 소수점 계산은 딱 떨어지지 않을 수 있어서 '오차범위(offset)'를 줍니다.
        assertThat(stockHolding.getAvgPrice()).isCloseTo(166.66, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("돈이 부족하면 매수에 실패해야 한다")
    void insufficientBalanceTest() {
        // Given
        User user = userRepository.save(new User("test@test.com", "12345", "test"));
        Account account = accountRepository.save(new Account(user));
        account.increaseBalance(200L); // 잔고 200원

        Stock stock = stockRepository.save(new Stock("KRW-BTC", "coin", 150.0)); // 가격 150원

        // When & Then
        // 150원짜리 2개 사려면 300원 필요 -> 200원밖에 없으니 예외 발생해야 함!
        // "이 람다식 안의 코드를 실행하면 IllegalStateException이 터져야 성공이다"
        assertThrows(IllegalStateException.class, () -> {
            orderService.buy(user.getId(), stock.getCode(), 2L);
        });
    }

    // ... 기존 buy 테스트 아래에 추가 ...

    @Test
    @DisplayName("주식 매도 성공: 잔액 증가, 보유량 감소, 주문 기록 생성")
    void sell_success() {
        // given
        // 1. 테스트를 위해 먼저 10개를 매수해둡니다.
        // (가정: 유저 100만원, 주식 1주당 1000원 -> 10개 매수)
        User user = userRepository.save(new User("test@test.com", "12345", "test"));
        Account account = accountRepository.save(new Account(user));
        account.increaseBalance(1000000L);

        Stock stock = stockRepository.save(new Stock("KRW-BTC", "coin", 1000.0));
        orderService.buy(user.getId(), stock.getCode(), 10L);

        // when
        // 2. 5개를 매도합니다. (현재가 1000원 가정 -> 5000원 벌어야 함)
        Long sellQuantity = 5L;
        orderService.sell(user.getId(), stock.getCode(), sellQuantity);

        // then
        // 3. 검증
        Account foundAccount = accountRepository.findByUserId(user.getId()).orElseThrow(); // 이건 왜 가져온거임?
        StockHolding holding = stockHoldingRepository.findByUserAndStock(userRepository.findById(user.getId()).get(), stockRepository.findById(stock.getCode()).get()).orElseThrow();

        // 잔액 확인: (원금 100만) - (매수 1만) + (매도 5천) = 99만 5천원
        // *주의: 테스트 환경에 따라 초기 잔액이 다를 수 있으니 로직에 맞춰 계산 필요
        // 여기서는 "매수 후 잔액"에서 "판 돈"만큼 늘었는지 확인하는 게 정확합니다.
        // 100만원(초기) - 1만원(매수) + 5천원(매도) = 99만 5천원
        assertThat(foundAccount.getBalance()).isEqualTo(995000L);
        assertThat(holding.getQuantity()).isEqualTo(5L); // 10개 - 5개 = 5개
    }

    @Test
    @DisplayName("주식 매도 실패: 보유 수량보다 많이 팔 수 없다")
    void sell_fail_not_enough_quantity() {
        // given
        // 1. 유저, 계좌, 종목 생성
        User user = userRepository.save(new User("fail1@test.com", "1234", "fail1"));
        Account account = accountRepository.save(new Account(user));
        account.increaseBalance(1000000L); // 돈은 넉넉히

        Stock stock = stockRepository.save(new Stock("KRW-ETH", "Ethereum", 2000.0));

        // 2. 10개 미리 매수
        orderService.buy(user.getId(), stock.getCode(), 10L);

        // when & then
        // 3. 100개 매도 시도 -> IllegalStateException (StockHolding.decreaseQuantity에서 발생)
        assertThrows(IllegalStateException.class, () -> {
            orderService.sell(user.getId(), stock.getCode(), 100L);
        });
    }

    @Test
    @DisplayName("주식 매도 실패: 보유하지 않은 주식은 팔 수 없다")
    void sell_fail_no_stock() {
        // given
        // 1. 유저, 계좌, 종목 생성
        User user = userRepository.save(new User("fail2@test.com", "1234", "fail2"));
        Account account = accountRepository.save(new Account(user));

        Stock stock = stockRepository.save(new Stock("KRW-DOGE", "Dogecoin", 100.0));

        // *중요* 매수(buy)를 하지 않음!

        // when & then
        // 2. 산 적 없는 주식 매도 시도 -> IllegalArgumentException (Service의 findByUserAndStock에서 발생)
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.sell(user.getId(), stock.getCode(), 1L);
        });
    }
}