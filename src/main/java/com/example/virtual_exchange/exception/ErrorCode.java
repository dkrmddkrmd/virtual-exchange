package com.example.virtual_exchange.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 유저 관련
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U001", "이미 가입된 이메일입니다."),

    // 공통/유효성 관련
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
    UPBIT_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Upbit 서버 오류가 발생했습니다."),
    Kafka_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "Kafka 서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}