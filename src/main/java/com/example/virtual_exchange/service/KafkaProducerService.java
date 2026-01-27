package com.example.virtual_exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    // KafkaTemplate: 스프링이 제공하는 '카프카 우체통'
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String message) {
        log.info("📤 [Kafka Producer] 전송할 메시지: topic={}, message={}", topic, message);

        // send(토픽이름, 보낼내용)
        kafkaTemplate.send(topic, message);
    }
}