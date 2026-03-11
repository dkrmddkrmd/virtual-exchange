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

    public Stock(String code, String name, Double currentPrice) {
        this.code = code;
        this.name = name;
        this.currentPrice = currentPrice;
    }

    public void updatePrice(Double newPrice) {
        this.currentPrice = newPrice;
    }
}
