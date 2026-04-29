package com.example.virtual_exchange.config.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60;

    public static void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true)       // 🔒 HTTPS 환경에서만 전송 허용 (SameSite=None 사용 시 필수)
                .sameSite("None")   // 🌍 크로스 도메인 (Vercel <-> Oracle) 쿠키 전송 허용
                .path("/")
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .build();

        // jakarta.servlet.http.Cookie 대신 헤더에 직접 문자열로 추가
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)          // 수명을 0으로 만들어서 즉시 만료시킴
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
