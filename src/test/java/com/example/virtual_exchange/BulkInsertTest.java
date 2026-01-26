package com.example.virtual_exchange;

import com.example.virtual_exchange.domain.Order;
import com.example.virtual_exchange.domain.OrderType;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.repository.OrderRepository;
import com.example.virtual_exchange.repository.StockRepository;
import com.example.virtual_exchange.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class BulkInsertTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StockRepository stockRepository;

    // 1. [핵심] 영속성 컨텍스트를 다루기 위해 매니저 소환
    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("DB에 데이터 100만 개 밀어넣기 (메모리 최적화 Ver)")
    @Rollback(false)
    void bulkInsert() {
        // 기존 유저/코인 재활용 (없으면 만들고, 있으면 가져오기 위해 try-catch 없앰)
        // 편의상 새로 만든다고 가정 (DB에 이미 있어도 ID 다르면 상관없음)
        User targetUser = userRepository.save(new User("target" + System.currentTimeMillis() + "@test.com", "1234", "타겟유저"));
        User dummyUser = userRepository.save(new User("dummy" + System.currentTimeMillis() + "@test.com", "1234", "더미유저"));
        Stock stock = stockRepository.save(new Stock("KRW-BTC", "비트코인", 50000.0));

        List<Order> buffer = new ArrayList<>();
        int totalRows = 1_000_000; // 100만 개 추가!

        System.out.println("🚀 데이터 삽입 시작...");
        long start = System.currentTimeMillis();

        for (int i = 1; i <= totalRows; i++) {
            User owner = (i % 100 == 0) ? targetUser : dummyUser;

            // 님의 생성자 스펙에 맞춰주세요
            Order order = new Order(owner, stock, OrderType.BUY, 50000.0, 1L);
            buffer.add(order);

            if (buffer.size() >= 1000) {
                orderRepository.saveAll(buffer);
                buffer.clear();

                // 2. [핵심 해결책] 1000개 넣을 때마다 메모리 비우기!
                em.flush(); // DB에 강제 전송
                em.clear(); // 영속성 컨텍스트(메모리) 비우기 -> 이걸 해야 안 터짐!

                // 로그로 진행 상황 확인
                if (i % 100000 == 0) {
                    System.out.println("현재 " + i + "개 저장 완료...");
                }
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("✅ 100만 건 저장 완료! 걸린 시간: " + (end - start) / 1000 + "초");
    }
}