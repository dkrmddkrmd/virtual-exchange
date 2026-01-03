package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.*;
import com.example.virtual_exchange.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional // (중요) 이 메소드 안의 모든 DB 작업은 한 몸이다! (하나라도 실패하면 롤백)
public class OrderService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;
    private final StockHoldingRepository stockHoldingRepository;
    private final OrderRepository orderRepository;

    public void buy(Long userId, String code, Long quantity) {
        // 1. 조회: 필요한 객체들을 가장 최신 상태로 가져온다.
        // (없으면 예외 발생시키는 게 안전함)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 유저입니다."));

        Stock stock = stockRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("없는 종목입니다."));

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("계좌가 없습니다."));

        // 2. 가격 계산 (Double -> Long 형변환 주의!)
        // 100.0 * 5 = 500.0 -> (long) 500
        long totalPrice = (long) (stock.getCurrentPrice() * quantity);

        // 3. 잔고 확인 (검증)
        if (account.getBalance() < totalPrice) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }

        // 4. 돈 빼기 (Account 변경)
        account.decreaseBalance(totalPrice);

        // 5. 주식 더하기 (StockHolding 변경) - [가장 어려운 부분!]
        // "이 유저가 이 주식을 가지고 있나?" 확인
        StockHolding holding = stockHoldingRepository.findByUserAndStock(user, stock)
                .orElse(null); // 없으면 null

        if (holding == null) {
            // (A) 없으면: 새로 만들어서 저장
            StockHolding newHolding = new StockHolding(user, stock, quantity, stock.getCurrentPrice());
            stockHoldingRepository.save(newHolding);
        } else {
            // (B) 있으면: 물타기 (수량 늘리고 평단가 수정)
            holding.addQuantity(quantity, stock.getCurrentPrice());
            // JPA 변경 감지(Dirty Checking) 덕분에 save 안 해도 자동 업데이트됨!
        }

        // 6. 기록 남기기 (Order 저장)
        Order order = new Order(user, stock, OrderType.BUY, stock.getCurrentPrice(), quantity);
        orderRepository.save(order);
    }

    public void sell(Long userId, String code, Long quantity) {
        // 1. 기본 조회 (User, Stock, Account) - OK
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 유저입니다."));
        Stock stock = stockRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("없는 종목입니다."));
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("계좌가 없습니다."));

        // 2. 보유 주식 조회 - OK
        StockHolding stockHolding = stockHoldingRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new IllegalArgumentException("매도할 주식이 없습니다."));

        // 4. 가격 계산 및 입금 - OK
        long totalPrice = (long) (stock.getCurrentPrice() * quantity);
        account.increaseBalance(totalPrice);

        // 5. 주식 차감
        stockHolding.decreaseQuantity(quantity); // (O) 가격 정보 필요 없음

        // 6. 기록 저장 - OK
        Order order = new Order(user, stock, OrderType.SELL, stock.getCurrentPrice(), quantity);
        orderRepository.save(order);
    }
}