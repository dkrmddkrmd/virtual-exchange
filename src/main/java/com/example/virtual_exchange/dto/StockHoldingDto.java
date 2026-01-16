package com.example.virtual_exchange.dto;

import com.example.virtual_exchange.domain.StockHolding;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockHoldingDto {
    private String stockName;
    private String stockCode;
    private Long quantity;
    private Double avgPrice;
    private Double currentPrice;
    private Double profitRate;

    // ★ [추가된 필드] 프론트엔드에서 이걸 기다리고 있습니다!
    private Long purchaseAmount;   // 총 매수 금액 (수량 * 평단가)
    private Long evaluationAmount; // 총 평가 금액 (수량 * 현재가)

    public StockHoldingDto(StockHolding entity) {
        this.stockName = entity.getStock().getName();
        this.stockCode = entity.getStock().getCode();
        this.quantity = entity.getQuantity();
        this.avgPrice = entity.getAvgPrice();
        this.currentPrice = entity.getStock().getCurrentPrice();

        // ★ [추가된 로직] 여기서 계산해서 넣어줘야 합니다.
        // 소수점 버리고 Long 타입으로 변환 (원화 기준)
        this.purchaseAmount = (long) (this.quantity * this.avgPrice);
        this.evaluationAmount = (long) (this.quantity * this.currentPrice);

        if (this.avgPrice > 0) {
            this.profitRate = (this.currentPrice - this.avgPrice) / this.avgPrice * 100;
        } else {
            this.profitRate = 0.0;
        }
    }
}