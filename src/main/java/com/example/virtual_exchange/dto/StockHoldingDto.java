package com.example.virtual_exchange.dto;

import com.example.virtual_exchange.domain.StockHolding;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class StockHoldingDto {
    private String stockName;
    private String stockCode;
    private Long quantity;
    private Double avgPrice;
    private Double currentPrice;
    private Double profitRate;

    private Long purchaseAmount;   // 총 매수 금액 (수량 * 평단가)
    private Long evaluationAmount; // 총 평가 금액 (수량 * 현재가)

    public StockHoldingDto(StockHolding entity) {
        this.stockName = entity.getStock().getName();
        this.stockCode = entity.getStock().getCode();
        this.quantity = entity.getQuantity();
        this.avgPrice = entity.getAvgPrice();
        this.currentPrice = entity.getStock().getCurrentPrice();

        this.purchaseAmount = (long) (this.quantity * this.avgPrice);
        this.evaluationAmount = (long) (this.quantity * this.currentPrice);

        if (this.avgPrice > 0) {
            this.profitRate = (this.currentPrice - this.avgPrice) / this.avgPrice * 100;
        } else {
            this.profitRate = 0.0;
        }
    }
}