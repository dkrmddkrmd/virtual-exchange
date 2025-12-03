package com.example.virtual_exchange.repository;

import com.example.virtual_exchange.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}