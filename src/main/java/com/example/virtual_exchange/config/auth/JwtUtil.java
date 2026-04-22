package com.example.virtual_exchange.config.auth;

import com.example.virtual_exchange.domain.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private static final long ACCESS_EXPIRATION  = 1000 * 60 * 30;          // 30분
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7일

    public JwtUtil(@Value("${jwt.secret}") String secretKeyString) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // 로그인 시 access token 발급 (subject = email)
    public String createAccessToken(Authentication authentication) {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return buildToken(principal.getUsername(), authorities, ACCESS_EXPIRATION);
    }

    // 재발급 시 access token 발급 (subject = email)
    public String createAccessTokenFromUser(User user) {
        return buildToken(user.getEmail(), user.getRole().name(), ACCESS_EXPIRATION);
    }

    // refresh token 발급 (subject = userId)
    public String createRefreshToken(Long userId) {
        return buildToken(String.valueOf(userId), null, REFRESH_EXPIRATION);
    }

    private String buildToken(String subject, String authorities, long expirationMs) {
        Date now = new Date();
        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(secretKey, Jwts.SIG.HS256);
        if (authorities != null) {
            builder.claim("auth", authorities);
        }
        return builder.compact();
    }

    // access token에서 email 추출 (JwtFilter 용)
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
    }

    // refresh token에서 userId 추출
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(
                Jwts.parser()
                        .verifyWith(secretKey).build()
                        .parseSignedClaims(token)
                        .getPayload().getSubject()
        );
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            log.info("잘못되거나 유효하지 않은 JWT 토큰입니다.");
        }
        return false;
    }
}
