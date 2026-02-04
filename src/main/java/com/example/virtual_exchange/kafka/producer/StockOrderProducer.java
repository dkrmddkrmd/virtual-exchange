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

            // [수정 후] userId를 Key로 설정 -> 같은 유저는 같은 파티션으로 -> 순서 보장!
            String key = String.valueOf(dto.getUserId());
            kafkaTemplate.send(TOPIC, key, jsonMessage);

            log.info("📤 [Producer] 주문 전송 (Key: {}): {}", key, jsonMessage);

        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패", e);
            throw new RuntimeException(e);
        }
    }
}