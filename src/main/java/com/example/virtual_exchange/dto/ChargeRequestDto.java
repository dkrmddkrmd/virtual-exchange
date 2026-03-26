package com.example.virtual_exchange.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChargeRequestDto {
    @Min(value = 1, message = "최소 1원 이상 요청해야 합니다.")
    private long money;
}
