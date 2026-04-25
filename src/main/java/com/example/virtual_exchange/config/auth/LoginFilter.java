package com.example.virtual_exchange.config.auth;

import com.example.virtual_exchange.domain.ActivityType;
import com.example.virtual_exchange.dto.LoginRequestDto;
import com.example.virtual_exchange.dto.TokenInfo;
import com.example.virtual_exchange.service.ActivityLogService;
import com.example.virtual_exchange.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final ActivityLogService activityLogService;

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService, ActivityLogService activityLogService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.activityLogService = activityLogService;
        setFilterProcessesUrl("/api/users/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequestDto loginDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());
            return authenticationManager.authenticate(token);
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 데이터를 읽는 데 실패했습니다.", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        PrincipalDetails principal = (PrincipalDetails) authResult.getPrincipal();
        Long userId = principal.getUser().getId();

        String accessToken  = jwtUtil.createAccessToken(authResult);
        String refreshToken = jwtUtil.createRefreshToken(userId);

        refreshTokenService.save(userId, refreshToken);
        CookieUtil.setRefreshCookie(response, refreshToken);

        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        activityLogService.saveActivityLog(userId, ActivityType.LOGIN, "로그인", ip);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String body = new ObjectMapper().writeValueAsString(new TokenInfo("Bearer", accessToken));
        response.getWriter().write(body);
    }
}
