package com.example.virtual_exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyAssetDto {
    private Long totalAssetAmount;
    private Long balance;
    private Long totalPurchaseAmount;
    private Long totalEvaluationAmount;
    private Long totalProfitLoss;
    private Double returnRate;

    private List<StockHoldingDto> holdingList;
}