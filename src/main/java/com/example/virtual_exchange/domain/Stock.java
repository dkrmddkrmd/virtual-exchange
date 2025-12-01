package com.example.virtual_exchange.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor()
public class Stock {
    @Id
    private String code;

    private String name;

    private Double currentPrice;

    // 1. 생성자 추가 (편의성)
    public Stock(String code, String name, Double currentPrice) {
        this.code = code;
        this.name = name;
        this.currentPrice = currentPrice;
    }

    // 2. 비즈니스 로직: 가격 업데이트
    // Setter 대신 이 메서드를 호출해서 가격을 바꿉니다.
    public void updatePrice(Double newPrice) {
        this.currentPrice = newPrice;
    }
}
