package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.*;
import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;
    private final StockHoldingRepository stockHoldingRepository;
    private final OrderRepository orderRepository;

    // [New] 외부에서 들어오는 유일한 창구
    public void createOrder(Long userId, OrderRequestDto dto) {
        // 1. DTO에서 주문 타입 확인 ("BUY" or "SELL")
        if ("BUY".equalsIgnoreCase(dto.getOrderType())) {
            // 2. 매수 로직 호출
            buy(userId, dto.getCode(), dto.getQuantity());
        } else if ("SELL".equalsIgnoreCase(dto.getOrderType())) {
            // 3. 매도 로직 호출
            sell(userId, dto.getCode(), dto.getQuantity());
        } else {
            // 4. 예외 처리 (BUY도 SELL도 아닌 이상한 값이 들어왔을 때)
            throw new IllegalArgumentException("잘못된 주문 타입입니다.");
        }
    }

    // 내부 로직 (이제 굳이 public일 필요가 없으므로 private으로 바꾸는 것을 추천합니다!)
    private void buy(Long userId, String code, Long quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 유저입니다."));
        Stock stock = stockRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("없는 종목입니다."));
        // ★ [수정] 다시 락 없는 일반 메서드로 변경!
        // (Redis가 앞에서 막아줄 거니까, DB 락은 풉니다)
        Account account = accountRepository.findByUserId(userId) // WithLock 아님!
                .orElseThrow(() -> new IllegalArgumentException("계좌가 없습니다."));

        long totalPrice = (long) (stock.getCurrentPrice() * quantity);

        if (account.getBalance() < totalPrice) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }

        account.decreaseBalance(totalPrice);

        StockHolding holding = stockHoldingRepository.findByUserAndStock(user, stock)
                .orElse(null);

        if (holding == null) {
            StockHolding newHolding = new StockHolding(user, stock, quantity, stock.getCurrentPrice());
            stockHoldingRepository.save(newHolding);
        } else {
            holding.addQuantity(quantity, stock.getCurrentPrice());
        }

        Order order = new Order(user, stock, OrderType.BUY, stock.getCurrentPrice(), quantity);
        orderRepository.save(order);
    }

    // 내부 로직 (private 추천)
    private void sell(Long userId, String code, Long quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 유저입니다."));
        Stock stock = stockRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("없는 종목입니다."));
        // ★ [수정] 다시 락 없는 일반 메서드로 변경!
        // (Redis가 앞에서 막아줄 거니까, DB 락은 풉니다)
        Account account = accountRepository.findByUserId(userId) // WithLock 아님!
                .orElseThrow(() -> new IllegalArgumentException("계좌가 없습니다."));

        StockHolding stockHolding = stockHoldingRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new IllegalArgumentException("매도할 주식이 없습니다."));

        // 매도 가능한지 수량 체크 로직이 StockHolding 도메인 안에 있었죠? (decreaseQuantity)
        // 거기서 예외를 던져주겠지만, 여기서 미리 체크해도 좋습니다.

        long totalPrice = (long) (stock.getCurrentPrice() * quantity);
        account.increaseBalance(totalPrice);

        stockHolding.decreaseQuantity(quantity);

        Order order = new Order(user, stock, OrderType.SELL, stock.getCurrentPrice(), quantity);
        orderRepository.save(order);
    }

    public List<OrderHistoryDto> getOrderLists(Long userId){

        return orderRepository.findAllByUserIdWithStock(userId).stream()
                .map(OrderHistoryDto::new)
                .toList();
    }

    // OrderService.java
    @Transactional(readOnly = true)
    public void getOrderLogs(Long userId) {
        // 1. 주문 목록 조회 (여기서는 쿼리 1방만 나감)
        List<Order> orders = orderRepository.findAllByUserId(userId);

        System.out.println("=== 🚨 [검증 시작] N+1 발생 구간 진입 ===");

        // 2. 반복문 돌면서 '지연 로딩' 강제 발생!
        for (Order order : orders) {
            // order.getStock().getName()을 호출하는 순간,
            // 가짜(Proxy) 종목 객체가 "어? 진짜 이름 필요하네?" 하고 DB를 찌릅니다.
            System.out.println("👉 주문ID: " + order.getId() + ", 종목명: " + order.getStock().getName());
        }

        System.out.println("=== 🚨 [검증 끝] N+1 발생 확인 완료 ===");
    }
}