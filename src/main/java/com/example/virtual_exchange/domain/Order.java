package com.example.virtual_exchange.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_date", columnList = "user_id, order_date DESC")
})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_code")
    private Stock stock;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    private Double price;
    private Long quantity;
    private LocalDateTime orderDate;

    public Order(User user, Stock stock, OrderType orderType, Double price, Long quantity) {
        this.user = user;
        this.stock = stock;
        this.orderType = orderType;
        this.price = price;
        this.quantity = quantity;
        this.orderDate = LocalDateTime.now();
    }
}