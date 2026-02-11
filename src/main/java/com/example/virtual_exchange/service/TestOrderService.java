package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.*;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestOrderService {
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;
    private final StockHoldingRepository stockHoldingRepository;
    private final OrderRepository orderRepository;

    // [수정] private -> public, @Transactional 추가
    // 이래야 실제 서비스 레이어처럼 트랜잭션이 동작해서 동시성 이슈가 재현됩니다.
    @Transactional
    public void buy(Long userId, OrderRequestDto requestDto) {
        Long quantity = requestDto.getQuantity();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 유저입니다."));
        Stock stock = stockRepository.findById(requestDto.getCode())
                .orElseThrow(() -> new IllegalArgumentException("없는 종목입니다."));

        // ★ 락 없는 일반 조회 (동시성 이슈 발생 지점)
        Account account = accountRepository.findByUserId(userId)
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
}