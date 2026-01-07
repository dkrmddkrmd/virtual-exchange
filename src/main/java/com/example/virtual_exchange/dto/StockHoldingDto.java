package com.example.virtual_exchange.dto;

import com.example.virtual_exchange.domain.StockHolding;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockHoldingDto {
    private String stockName;   // 종목명 (Stock에서 꺼내옴)
    private String stockCode;   // 종목코드
    private Long quantity;      // 보유수량
    private Double avgPrice;    // 평단가
    private Double currentPrice;// 현재가
    private Double profitRate;  // 수익률 (옵션)

    // Entity -> DTO 변환 생성자
    public StockHoldingDto(StockHolding entity) {
        this.stockName = entity.getStock().getName(); // 여기서 지연 로딩 초기화됨 (쿼리 발생)
        this.stockCode = entity.getStock().getCode();
        this.quantity = entity.getQuantity();
        this.avgPrice = entity.getAvgPrice();
        this.currentPrice = entity.getStock().getCurrentPrice();

        // 수익률 단순 계산 예시
        if (this.avgPrice > 0) {
            this.profitRate = (this.currentPrice - this.avgPrice) / this.avgPrice * 100;
        } else {
            this.profitRate = 0.0;
        }
    }
}