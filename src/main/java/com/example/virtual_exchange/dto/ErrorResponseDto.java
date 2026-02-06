package com.example.virtual_exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    private String code;    // 에러 코드 (예: "BAD_REQUEST", "NOT_FOUND")
    private String message; // 상세 메시지 (예: "잔액이 부족합니다.")
}