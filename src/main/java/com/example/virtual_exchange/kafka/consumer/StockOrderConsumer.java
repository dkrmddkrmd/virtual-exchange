package com.example.virtual_exchange.kafka.consumer;

import com.example.virtual_exchange.domain.*;
import com.example.virtual_exchange.dto.OrderMessageDto;
import com.example.virtual_exchange.repository.*;
import com.example.virtual_exchange.service.OrderTransactionService;
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
    private final ObjectMapper objectMapper;
    private final OrderTransactionService orderTransactionService;

    @KafkaListener(topics = "stock_order", groupId = "stock-group-v6", concurrency = "3")
    public void consumeOrder(String messageJson, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        try {
            OrderMessageDto message = objectMapper.readValue(messageJson, OrderMessageDto.class);

            log.info("🔥 [Consumer] 처리 시작 (Partition: {}, User: {})", partition, message.getUserId());

            if ("BUY".equalsIgnoreCase(message.getOrderType())) {
                orderTransactionService.buy(message.getUserId(), message.getCode(), message.getQuantity());
            } else if ("SELL".equalsIgnoreCase(message.getOrderType())) {
                orderTransactionService.sell(message.getUserId(), message.getCode(), message.getQuantity());
            } else {
                log.warn("⚠️ 잘못된 주문 타입: {}", message.getOrderType());
            }

        } catch (IllegalArgumentException | IllegalStateException | JsonProcessingException e) {
            log.warn("⛔ [처리 불가 - 재시도 X] 사유: {}", e.getMessage());

        } catch (Exception e) {
            log.error("🚨 [시스템 오류 - 재시도 O]", e);
            throw new RuntimeException(e);
        }
    }
}