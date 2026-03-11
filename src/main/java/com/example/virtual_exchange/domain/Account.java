package com.example.virtual_exchange.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Long balance;

    public Account(User user) {
        this.user = user;
        this.balance = 0L; // 초기 잔액 0원
    }

    public void increaseBalance(Long amount) {
        this.balance += amount;
    }

    public void decreaseBalance(Long amount) {
        if (this.balance < amount) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
        this.balance -= amount;
    }
}
