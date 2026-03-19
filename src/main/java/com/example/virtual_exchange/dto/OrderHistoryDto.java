package com.example.virtual_exchange.dto;

import com.example.virtual_exchange.domain.Order;
import com.example.virtual_exchange.domain.OrderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryDto {
    private Long orderId;
    private String stockName;
    private String orderType;
    private Long quantity;
    private Double price;       // 1주당 가격
    private Double totalAmount; // 총 거래 금액
    private String orderDate;

    public OrderHistoryDto(Order order) {
        this.orderId = order.getId();
        this.stockName = order.getStock().getName();
        this.orderType = (order.getOrderType() == OrderType.BUY) ? "매수" : "매도";
        this.quantity = order.getQuantity();
        this.price = order.getPrice();
        this.totalAmount = order.getPrice() * order.getQuantity();
        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}