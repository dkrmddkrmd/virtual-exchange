package com.example.virtual_exchange.kafka.consumer;

import com.example.virtual_exchange.domain.*;
import com.example.virtual_exchange.kafka.dto.OrderMessageDto;
import com.example.virtual_exchange.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockOrderConsumer {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;
    private final StockHoldingRepository stockHoldingRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    // [트랜잭션 시작점]
    @Transactional
    @KafkaListener(topics = "stock_order", groupId = "stock-group-v3", concurrency = "3")
    public void consumeOrder(String messageJson, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        try {
            OrderMessageDto message = objectMapper.readValue(messageJson, OrderMessageDto.class);
            log.info("🔥 [Consumer] 처리 시작 (Partition: {}, User: {})", partition, message.getUserId());

            if ("BUY".equalsIgnoreCase(message.getOrderType())) {
                buy(message.getUserId(), message.getCode(), message.getQuantity());
            } else if ("SELL".equalsIgnoreCase(message.getOrderType())) {
                sell(message.getUserId(), message.getCode(), message.getQuantity());
            } else {
                log.warn("⚠️ 잘못된 주문 타입: {}", message.getOrderType());
            }

        } catch (IllegalArgumentException | IllegalStateException | JsonProcessingException e) {
            log.warn("⛔ [처리 불가 - 재시도 X] 사유: {}", e.getMessage());

        } catch (Exception e) {
            log.error("🚨 [시스템 오류 - 재시도 O]", e);
            throw new RuntimeException(e); // DB 에러 등은 여기서 잡혀서 재시도됨
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
}