package com.example.virtual_exchange.dto;

import com.example.virtual_exchange.domain.Stock;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockResponseDto {
    private String code;
    private String name;
    private Double price;

    // Entity -> DTO 변환 생성자
    public StockResponseDto(Stock stock) {
        this.code = stock.getCode();
        this.name = stock.getName();
        this.price = stock.getCurrentPrice();
    }
}