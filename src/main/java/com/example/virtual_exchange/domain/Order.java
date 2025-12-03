package com.example.virtual_exchange.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "orders") // 주의: 'ORDER'는 DB 예약어라 에러남! 'orders'로 변경 필수
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 한 유저가 주문을 수백 번 할 수 있음
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // 한 종목에 주문이 수천 개 쌓일 수 있음
    @JoinColumn(name = "stock_code")
    private Stock stock;

    @Enumerated(EnumType.STRING) // DB에 숫자(0,1) 대신 글자("BUY")로 저장
    private OrderType orderType;

    private Double price;    // 당시 체결 가격
    private Long quantity;   // 주문 수량
    private LocalDateTime orderDate; // 주문 시간

    // 생성자
    public Order(User user, Stock stock, OrderType orderType, Double price, Long quantity) {
        this.user = user;
        this.stock = stock;
        this.orderType = orderType;
        this.price = price;
        this.quantity = quantity;
        this.orderDate = LocalDateTime.now(); // 생성 시점 시간 자동 저장
    }
}