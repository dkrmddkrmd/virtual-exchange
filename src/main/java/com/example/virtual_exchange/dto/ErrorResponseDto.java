package com.example.virtual_exchange.dto;

import com.example.virtual_exchange.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponseDto {
    private String code;
    private String message;

    // ErrorCode를 받아서 Dto를 만드는 정적 팩토리 메서드
    public static ErrorResponseDto of(ErrorCode errorCode) {
        return new ErrorResponseDto(errorCode.getCode(), errorCode.getMessage());
    }

    // 상세 메시지가 필요한 경우 (예: "이미 가입된 이메일입니다: test@test.com")
    public static ErrorResponseDto of(ErrorCode errorCode, String detailMessage) {
        return new ErrorResponseDto(errorCode.getCode(), detailMessage);
    }
}