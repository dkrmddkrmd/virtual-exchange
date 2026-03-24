package com.example.virtual_exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenInfo {
    private String grantType;
    private String accessToken; // 실제 JWT 암호화 토큰 문자열
}