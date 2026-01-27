package com.example.virtual_exchange.kafka.producer;

import com.example.virtual_exchange.kafka.dto.OrderMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockOrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 토픽 이름을 여기서 상수로 관리 (Service가 몰라도 됨)
    private static final String TOPIC = "stock_order";

    public void sendOrder(OrderMessageDto dto) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(dto);

            kafkaTemplate.send(TOPIC, jsonMessage);
            log.info("📤 [StockOrderProducer] 주문 전송 완료: {}", jsonMessage);

        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패", e);
        }
    }
}