package com.example.virtual_exchange.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderRequestDto {
    private Long userId;
    private String code;
    private Long quantity;

    public OrderRequestDto(Long userId, String code, Long quantity) {
        this.userId = userId;
        this.code = code;
        this.quantity = quantity;
    }
}
