package com.example.virtual_exchange.repository;

import com.example.virtual_exchange.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 2. 거래내역 화면 전용 조회 (Fetch Join 적용!)
    // o.stock을 가져올 때 join fetch를 써서 한방에 가져오라고 명시
    @Query("select o from Order o join fetch o.stock where o.user.id = :userId order by o.orderDate desc")
    List<Order> findAllByUserIdWithStock(@Param("userId") Long userId);
    // 🚨 [BAD] 아무 옵션 없는 일반 조회 (N+1 발생 후보)
    List<Order> findAllByUserId(Long userId);
}