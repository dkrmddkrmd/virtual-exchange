package com.example.virtual_exchange.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 API 주소(/**)에 대하여
                .allowedOrigins("http://localhost:5173") // 리액트 서버(5173)의 접근을 허락
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허락할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 쿠키나 인증 정보도 허용 (나중에 로그인 붙일 때 필수)
    }
}