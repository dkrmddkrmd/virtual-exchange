package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.service.OrderService;
import com.example.virtual_exchange.facade.RedissonLockStockFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/orders") // 테스트용 URL 분리
public class TestOrderController {

    private final OrderService orderService;
    private final RedissonLockStockFacade redissonLockStockFacade;

    // [CASE 1] Redis 분산 락 적용 (동기 방식 + 락 대기) -> 느림 & 정합성 O
    // 사용법: POST /api/test/orders/redis/1 (유저 ID 1번)
    @PostMapping("/redis/{userId}")
    public ResponseEntity<String> createOrderWithRedisLock(
            @PathVariable Long userId,
            @RequestBody OrderRequestDto requestDto) {

        // Facade가 락을 걸고 -> "동기적"으로 DB에 저장하는 로직을 호출해야 함
        redissonLockStockFacade.createOrder(userId, requestDto);

        return ResponseEntity.ok("Redis 분산 락 주문 성공");
    }

    // [CASE 2] Kafka 비동기 적용 (락 제거) -> 빠름 & 정합성 O (순서 보장)
    // 사용법: POST /api/test/orders/kafka/1 (유저 ID 1번)
    @PostMapping("/kafka/{userId}")
    public ResponseEntity<String> createOrderWithKafka(
            @PathVariable Long userId,
            @RequestBody OrderRequestDto requestDto) {

        // Kafka Producer 호출 (순식간에 리턴됨)
        orderService.createOrder(userId, requestDto);

        return ResponseEntity.ok("Kafka 비동기 주문 접수 완료");
    }
}