package com.example.virtual_exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyAssetDto {
    private Long totalAssetAmount;
    private Long balance;
    private Long totalPurchaseAmount;
    private Long totalEvaluationAmount;
    private Long totalProfitLoss;
    private Double returnRate; // Long -> Double 변경 (수익률은 소수점)
}
