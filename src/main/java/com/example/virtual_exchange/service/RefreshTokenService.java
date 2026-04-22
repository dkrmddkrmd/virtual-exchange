package com.example.virtual_exchange.service;

import com.example.virtual_exchange.config.auth.JwtUtil;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.exception.InvalidRefreshTokenException;
import com.example.virtual_exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    private static final String PREFIX = "refresh:";
    private static final Duration TTL = Duration.ofDays(7);

    public record ReissueResult(String accessToken, String refreshToken) {}

    public void save(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(PREFIX + userId, refreshToken, TTL);
    }

    public void delete(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }

    public Long extractUserId(String token) {
        return parseUserId(token);
    }

    public ReissueResult rotate(String incomingToken) {
        Long userId = parseUserId(incomingToken);
        validateAgainstRedis(userId, incomingToken);

        User user = userRepository.findById(userId)
                .orElseThrow(InvalidRefreshTokenException::new);

        String newAccessToken  = jwtUtil.createAccessTokenFromUser(user);
        String newRefreshToken = jwtUtil.createRefreshToken(userId);

        save(userId, newRefreshToken);

        return new ReissueResult(newAccessToken, newRefreshToken);
    }

    private Long parseUserId(String token) {
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new InvalidRefreshTokenException();
        }
    }

    private void validateAgainstRedis(Long userId, String incomingToken) {
        String stored = redisTemplate.opsForValue().get(PREFIX + userId);
        if (stored == null || !stored.equals(incomingToken)) {
            delete(userId); // 토큰 탈취 의심 → 기존 세션 전부 무효화
            throw new InvalidRefreshTokenException();
        }
    }
}
