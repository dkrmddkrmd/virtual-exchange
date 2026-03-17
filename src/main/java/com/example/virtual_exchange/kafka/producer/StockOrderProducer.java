package com.example.virtual_exchange.kafka.producer;

import com.example.virtual_exchange.dto.OrderMessageDto;
import com.example.virtual_exchange.exception.KafkaProducerErrorException;
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

    private static final String TOPIC = "stock_order";

    public void sendOrder(OrderMessageDto dto) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(dto);
            String key = String.valueOf(dto.getUserId());

            //.get()을 호출해서 카프카 브로커의 응답을 끝까지 기다림
            kafkaTemplate.send(TOPIC, key, jsonMessage).get();

            log.info("📤 [Producer] 주문 전송 완료 (Key: {}): {}", key, jsonMessage);

        } catch (JsonProcessingException e) {
            throw new KafkaProducerErrorException("주문 데이터 변환 중 오류가 발생했습니다.");
        } catch (Exception e) {
            //.get() 대기 중에 카프카가 죽어있거나 타임아웃이 나면 이리로 빠집니다
            log.error("🚨 [Producer] 주문 전송 실패 (Key: {}): {}", dto.getUserId(), e.getMessage());
            throw new KafkaProducerErrorException("카프카 서버로 주문을 전송하는 데 실패했습니다.");
        }
    }
}