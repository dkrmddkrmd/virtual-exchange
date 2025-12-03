package com.example.virtual_exchange.repository;

import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.StockHolding; // Import 확인!
import com.example.virtual_exchange.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    Optional<StockHolding> findByUserAndStock(User user, Stock stock);
}