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

    public StockResponseDto(Stock stock) {
        this.code = stock.getCode();
        this.name = stock.getName();
        this.price = stock.getCurrentPrice();
    }
}