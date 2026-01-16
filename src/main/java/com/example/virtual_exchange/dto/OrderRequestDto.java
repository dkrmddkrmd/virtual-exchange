package com.example.virtual_exchange.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderRequestDto {

    @NotBlank(message = "종목 코드는 필수입니다.")
    private String code;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 최소 1주 이상이어야 합니다.") // ★ 핵심 방어 코드
    private Long quantity;

    @NotBlank(message = "주문 타입(BUY/SELL)은 필수입니다.")
    private String orderType;

    public OrderRequestDto(String code, Long quantity, String orderType) {
        this.code = code;
        this.quantity = quantity;
        this.orderType = orderType;
    }
}
