package com.example.virtual_exchange.repository;

import com.example.virtual_exchange.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 2. 거래내역 화면 전용 조회 (Fetch Join 적용!)
    // o.stock을 가져올 때 join fetch를 써서 한방에 가져오라고 명시
//    @Query("select o from Order o join fetch o.stock where o.user.id = :userId order by o.orderDate desc")
//    List<Order> findAllByUserIdWithStock(@Param("userId") Long userId);

    // [변경 후] Pageable 추가 + 반환 타입 Page<Order>로 변경
    // countQuery: 페이징을 하려면 전체 개수를 알아야 해서 카운트 쿼리가 필요한데,
    // join fetch를 쓰면 카운트 쿼리 짤 때 꼬일 수 있어서 countQuery를 따로 명시해주는 게 안전합니다.
    @Query(value = "select o from Order o join fetch o.stock where o.user.id = :userId order by o.orderDate desc",
            countQuery = "select count(o) from Order o where o.user.id = :userId")
    Page<Order> findAllByUserIdWithStock(@Param("userId") Long userId, Pageable pageable);
}