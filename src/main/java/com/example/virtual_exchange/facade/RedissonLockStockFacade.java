package com.example.virtual_exchange.facade;

import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.service.OrderService;
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

    private final RedissonClient redissonClient;
    private final OrderService orderService;

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
                log.info("락 획득 실패");
                return;
            }

            // 3. ★ 진짜 비즈니스 로직 실행 (주문)
            orderService.createOrder(userId, requestDto);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 4. 락 해제 (무조건 실행)
            // 내가 락을 잡고 있을 때만 해제해야 함
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}