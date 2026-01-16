package com.example.virtual_exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyAssetDto {
    private Long totalAssetAmount;
    private Long balance;
    private Long totalPurchaseAmount;
    private Long totalEvaluationAmount;
    private Long totalProfitLoss;
    private Double returnRate;

    // ★ [수정 필수] stockHoldings -> holdingList 로 변경!
    // (JS가 data.holdingList 라고 부르고 있기 때문입니다)
    private List<StockHoldingDto> holdingList;
}