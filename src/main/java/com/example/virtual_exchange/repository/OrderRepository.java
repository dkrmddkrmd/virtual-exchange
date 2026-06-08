package com.example.virtual_exchange.repository;

import com.example.virtual_exchange.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. 커서가 없을 때 (첫 페이지 조회)
    // ORDER BY o.id DESC를 사용합니다. (보통 id가 생성 순서대로 커지므로 orderDate와 동일한 정렬 효과를 가집니다)
    @Query("SELECT o FROM Order o JOIN FETCH o.stock " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.id DESC")
    List<Order> findFirstPageByUserId(@Param("userId") Long userId, Pageable pageable);

    // 2. 커서가 있을 때 (두 번째 페이지부터 조회)
    // 핵심: o.id < :lastOrderId 조건으로 앞선 데이터를 완벽히 건너뜁니다!
    @Query("SELECT o FROM Order o JOIN FETCH o.stock " +
            "WHERE o.user.id = :userId AND o.id < :lastOrderId " +
            "ORDER BY o.id DESC")
    List<Order> findNextPageByUserId(@Param("userId") Long userId, @Param("lastOrderId") Long lastOrderId, Pageable pageable);
}