package com.example.virtual_exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpbitTickerDto {
    private String market;
    private Double trade_price;
}
