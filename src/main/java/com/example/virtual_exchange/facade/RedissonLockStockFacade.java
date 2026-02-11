package com.example.virtual_exchange.facade;

import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.service.OrderService;
import com.example.virtual_exchange.service.TestOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedissonLockStockFacade {
    // 지금 쓰이진 않지만 일단 공부용

    private final RedissonClient redissonClient;
    //private final OrderService orderService;
    private final TestOrderService testOrderService;

    public void createOrder(Long userId, OrderRequestDto requestDto) {
        // 1. 락 이름 정의 (사용자 ID별로 락을 검 -> 내 계좌만 잠금)
        String lockKey = "lock:user:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 2. 락 획득 시도 (tryLock)
            // waitTime: 락을 얻을 때까지 기다리는 시간 (10초)
            // leaseTime: 락을 얻고 나서 잡고 있는 시간 (1초 지나면 강제 반납 -> 데드락 방지)
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

            if (!available) {
                log.warn("[Lock 실패] 유저 {} 락 획득 실패 - 트래픽 과부하", userId);
                throw new IllegalStateException("현재 주문량이 많아 처리가 지연되고 있습니다.");
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            testOrderService.buy(userId, requestDto);

            // 3. ★ 진짜 비즈니스 로직 실행 (주문)
            //orderService.createOrder(userId, requestDto);

        } catch (InterruptedException e) {
            log.error("[Lock 에러] 유저 {} 락 획득 중 인터럽트 발생", userId, e);
            Thread.currentThread().interrupt(); // 스레드 상태 복구
            throw new IllegalStateException("서버 에러가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}