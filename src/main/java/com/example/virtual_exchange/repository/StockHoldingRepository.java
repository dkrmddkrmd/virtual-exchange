package com.example.virtual_exchange.repository;

import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.StockHolding; // Import 확인!
import com.example.virtual_exchange.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    Optional<StockHolding> findByUserAndStock(User user, Stock stock);

    // sh(보유주식)을 가져올 때, sh.stock(종목정보)도 같이(FETCH) 가져오라는 뜻
    @Query("SELECT sh FROM StockHolding sh JOIN FETCH sh.stock WHERE sh.user.id = :userId")
    List<StockHolding> findAllByUserId(@Param("userId") Long userId);
}