package com.example.virtual_exchange.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final PrincipalDetailsService principalDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 헤더 꺼내기
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " 7글자 자르고 순수 토큰만 추출
        String token = authorizationHeader.substring(7);

        // 토큰 검사
        if (jwtUtil.validateToken(token)) {
            String email = jwtUtil.getEmailFromToken(token);

            UserDetails userDetails = principalDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken securityToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // (선택) 통행증에 접속한 IP 주소 같은 디테일 정보도 예쁘게 적어줍니다.
            securityToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // (SecurityContextHolder)에 통행증 안착
            SecurityContextHolder.getContext().setAuthentication(securityToken);
        }

        // 할 일 끝났으니 다음 문지기(필터)로 넘겨줌
        filterChain.doFilter(request, response);
    }
}