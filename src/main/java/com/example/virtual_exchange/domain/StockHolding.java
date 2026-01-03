package com.example.virtual_exchange.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class StockHolding {

    @Id // 고민 해결: 그냥 숫자 ID 쓰세요!
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // (중요) 한 사람이 여러 종목을 가질 수 있음!
    @JoinColumn(name = "user_id")      // user_id 컬럼과 연결
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // (중요) 한 종목을 여러 사람이 가질 수 있음!
    @JoinColumn(name = "stock_code")   // Stock의 PK는 code였죠?
    private Stock stock;

    private Long quantity; // 보유 수량

    private Double avgPrice; // 평단가 (매수 평균가)

    public StockHolding(User user, Stock stock, Long quantity, Double avgPrice) {
        this.user = user;
        this.stock = stock;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
    }

    // 비즈니스 로직: 추가 매수 시 평단가/수량 업데이트
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