package com.example.virtual_exchange.dto;

import com.example.virtual_exchange.domain.Order;
import com.example.virtual_exchange.domain.OrderType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

// OrderHistoryDto.java
@Getter
@NoArgsConstructor
public class OrderHistoryDto {
    private Long orderId;
    private String stockName;
    private String orderType; // "매수" 또는 "매도"로 변환해서 넣기
    private Long quantity;
    private Double price;       // 1주당 가격
    private Double totalAmount; // 총 거래 금액
    private String orderDate; // "2024-01-06 15:30" 형식

    public OrderHistoryDto(Order order) {
        this.orderId = order.getId();
        this.stockName = order.getStock().getName();
        // ENUM -> 한글 변환
        this.orderType = (order.getOrderType() == OrderType.BUY) ? "매수" : "매도";
        this.quantity = order.getQuantity();
        this.price = order.getPrice();
        this.totalAmount = order.getPrice() * order.getQuantity();
        // 날짜 포맷팅 (나중에 Service에서 하거나 여기서 처리)
        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}