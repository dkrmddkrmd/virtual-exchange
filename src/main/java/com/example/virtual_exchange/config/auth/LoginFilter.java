package com.example.virtual_exchange.config.auth;

import com.example.virtual_exchange.dto.LoginRequestDto;
import com.example.virtual_exchange.dto.TokenInfo;
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

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/users/login"); // 리액트가 POST로 쏠 주소
    }

    // 💡 1. 낚아채기: 리액트의 요청을 받아서 검증을 시작하는 곳
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 💡 readValue: 리액트가 request 바디에 담아 보낸 JSON을 LoginRequestDto로 쏙 변환!
            LoginRequestDto loginDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);

            // 💡 꺼낸 이메일과 비밀번호로 임시 통행증 만들기
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

            // 💡 매니저에게 "이 통행증 진짜인지 DB랑 확인해 줘!" 던지기
            return authenticationManager.authenticate(token);

        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 데이터를 읽는 데 실패했습니다.", e);
        }
    }

    // 💡 2. 로그인 성공 시: 매니저가 "비밀번호 맞아!" 하면 여기가 실행됨
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        // 💡 아까 만든 토큰 공장(JwtUtil) 가동!
        TokenInfo tokenInfo = jwtUtil.createToken(authResult);

        // 💡 리액트가 알아먹을 수 있게 JSON 형태로 응답(Response) 세팅
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 💡 이번에는 writeValueAsString! (자바 객체인 TokenInfo를 JSON 문자열로 변환)
        ObjectMapper objectMapper = new ObjectMapper();
        String resultJson = objectMapper.writeValueAsString(tokenInfo);

        // 💡 화면(응답 바디)에 출력!
        response.getWriter().write(resultJson);
    }
}