package com.example.virtual_exchange.repository;

import com.example.virtual_exchange.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Pageable 추가 + 반환 타입 Page<Order>로 변경
    @Query(value = "select o from Order o join fetch o.stock where o.user.id = :userId order by o.orderDate desc",
            countQuery = "select count(o) from Order o where o.user.id = :userId")
    Page<Order> findAllByUserIdWithStock(@Param("userId") Long userId, Pageable pageable);
}