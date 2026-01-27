package com.example.virtual_exchange.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class KafkaConsumerService {

    // 📬 Kafka 리스너: "stock_order" 토픽에 메시지가 오면 이 메서드가 실행됨
    // groupId = "stock-group" (아까 설정파일에 적은 그 팀 이름)
    @KafkaListener(topics = "stock_order", groupId = "stock-group")
    public void consume(String message) throws IOException {

        // 1. 메시지 도착 확인
        log.info("📥 [Kafka Consumer] 쪽지 도착! 내용: {}", message);

        try {
            // 2. 무거운 작업 시뮬레이션 (2초 대기)
            // (마치 이메일 보내거나, 복잡한 통계 계산을 하는 척)
            Thread.sleep(2000);

            // 3. 작업 완료
            log.info("✅ [Kafka Consumer] 2초 뒤 작업 완료! (알림 발송 등)");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}