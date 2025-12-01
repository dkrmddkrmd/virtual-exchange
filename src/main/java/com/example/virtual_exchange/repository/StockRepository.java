package com.example.virtual_exchange.repository;

import com.example.virtual_exchange.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, String> {

}
