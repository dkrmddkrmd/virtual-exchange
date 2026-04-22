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
            orderRepository.save(new Order(user, stock, OrderType.BUY, stock.getCurrentPrice(), quantity, "잔액이 부족합니다."));
            log.warn("⛔ [매수 실패] User: {}, 종목: {}, 사유: 잔액 부족", userId, code);
            return;
        }

        account.decreaseBalance(totalPrice);

        StockHolding holding = stockHoldingRepository.findByUserAndStock(user, stock).orElse(null);
        if (holding == null) {
            stockHoldingRepository.save(new StockHolding(user, stock, quantity, stock.getCurrentPrice()));
        } else {
            holding.addQuantity(quantity, stock.getCurrentPrice());
        }

        orderRepository.save(new Order(user, stock, OrderType.BUY, stock.getCurrentPrice(), quantity));
        log.info("✅ [매수 완료] User: {}, 종목: {}, 수량: {}, 총액: {}", userId, code, quantity, totalPrice);
    }

    public void sell(Long userId, String code, Long quantity) {
        User user = getUser(userId);
        Stock stock = getStock(code);
        Account account = getAccount(userId);

        StockHolding stockHolding = stockHoldingRepository.findByUserAndStock(user, stock).orElse(null);

        if (stockHolding == null) {
            orderRepository.save(new Order(user, stock, OrderType.SELL, stock.getCurrentPrice(), quantity, "보유한 주식이 없습니다."));
            log.warn("⛔ [매도 실패] User: {}, 종목: {}, 사유: 보유 주식 없음", userId, code);
            return;
        }

        if (stockHolding.getQuantity() < quantity) {
            orderRepository.save(new Order(user, stock, OrderType.SELL, stock.getCurrentPrice(), quantity, "보유 수량이 부족합니다."));
            log.warn("⛔ [매도 실패] User: {}, 종목: {}, 사유: 보유 수량 부족", userId, code);
            return;
        }

        long totalPrice = (long) (stock.getCurrentPrice() * quantity);
        account.increaseBalance(totalPrice);
        stockHolding.decreaseQuantity(quantity);

        orderRepository.save(new Order(user, stock, OrderType.SELL, stock.getCurrentPrice(), quantity));
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
