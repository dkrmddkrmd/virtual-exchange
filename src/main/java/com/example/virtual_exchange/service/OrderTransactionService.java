package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.*;
import com.example.virtual_exchange.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class OrderTransactionService {
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;
    private final StockHoldingRepository stockHoldingRepository;
    private final OrderRepository orderRepository;

    public void buy(Long userId, String code, Long quantity) {
        User user = getUser(userId);
        Stock stock = getStock(code);
        Account account = getAccount(userId);

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

        log.info("✅ [매수 완료] User: {}, 종목: {}, 수량: {}, 총액: {}", userId, code, quantity, totalPrice);
    }

    public void sell(Long userId, String code, Long quantity) {
        User user = getUser(userId);
        Stock stock = getStock(code);
        Account account = getAccount(userId);

        StockHolding stockHolding = stockHoldingRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new IllegalArgumentException("매도할 주식이 없습니다."));

        if(stockHolding.getQuantity() < quantity) {
            throw new IllegalArgumentException("보유 주식 개수가 부족합니다.");
        }

        long totalPrice = (long) (stock.getCurrentPrice() * quantity);
        account.increaseBalance(totalPrice);

        stockHolding.decreaseQuantity(quantity);

        Order order = new Order(user, stock, OrderType.SELL, stock.getCurrentPrice(), quantity);
        orderRepository.save(order);

        log.info("✅ [매도 완료] User: {}, 종목: {}, 수량: {}, 총액: {}", userId, code, quantity, totalPrice);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 유저입니다."));
    }

    private Stock getStock(String code) {
        return stockRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("없는 종목입니다."));
    }

    private Account getAccount(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("계좌가 없습니다."));
    }
}
