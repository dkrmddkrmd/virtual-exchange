package com.example.virtual_exchange.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class StockHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_code")
    private Stock stock;

    private Long quantity;

    private Double avgPrice;

    public StockHolding(User user, Stock stock, Long quantity, Double avgPrice) {
        this.user = user;
        this.stock = stock;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
    }

    //추가 매수 시 평단가/수량 업데이트
    public void addQuantity(Long amount, Double price) {
        // 평단가 계산 로직: ((기존수량 * 기존평단) + (새수량 * 새가격)) / 전체수량
        double totalCost = (this.quantity * this.avgPrice) + (amount * price);
        this.quantity += amount;
        this.avgPrice = totalCost / this.quantity;
    }

    public void decreaseQuantity(Long amount) {
        if (this.quantity < amount) {
            throw new IllegalStateException("보유 수량이 부족하여 매도할 수 없습니다.");
        }
        this.quantity -= amount;
    }
}