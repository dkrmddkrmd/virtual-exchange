package com.example.virtual_exchange.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderRequestDto {
    private String code;
    private Long quantity;
    private String orderType; // "BUY" or "SELL"

    public OrderRequestDto(String code, Long quantity, String orderType) {
        this.code = code;
        this.quantity = quantity;
        this.orderType = orderType;
    }
}
