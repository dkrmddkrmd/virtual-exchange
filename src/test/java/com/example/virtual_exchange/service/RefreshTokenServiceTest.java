package com.example.virtual_exchange.service;

import com.example.virtual_exchange.config.auth.JwtUtil;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.exception.InvalidRefreshTokenException;
import com.example.virtual_exchange.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {
    @Mock StringRedisTemplate redisTemplate;
    @Mock JwtUtil jwtUtil;
    @Mock UserRepository userRepository;
    @Mock ValueOperations<String, String> valueOps;

    @InjectMocks RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("rotate에 성공하면 새로운 토큰 발급 및 Redis가 갱신되어야 한다.")
    public void Rotate_성공_테스트(){
        //Given
        User user = new User("test@test.com", "1234", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Long userId = user.getId();
        String givenToken = "aaa";
        String accessToken = "abc";
        String refreshToken = "efg";


        // ValueOperations도 Mock 필요
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("refresh:1")).thenReturn(givenToken);
        Mockito.when(jwtUtil.getUserIdFromToken(givenToken)).thenReturn(userId);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(jwtUtil.createAccessTokenFromUser(user)).thenReturn(accessToken);
        Mockito.when(jwtUtil.createRefreshToken(userId)).thenReturn(refreshToken);

        //When
        RefreshTokenService.ReissueResult result = refreshTokenService.rotate(givenToken);

        Assertions.assertThat(result.accessToken()).isEqualTo(accessToken);
        Assertions.assertThat(result.refreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("이미 갱신된 토큰으로 rotate 시도 시 예외가 발생해야 한다.")
    public void Rotate_실패_테스트() {
        // Given
        Long userId = 1L;
        String oldToken = "old-token";   // 해커가 탈취한 토큰
        String newToken = "new-token";   // Redis에 저장된 최신 토큰

        Mockito.when(jwtUtil.getUserIdFromToken(oldToken)).thenReturn(userId);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOps);
        // Redis에는 새 토큰이 있음
        Mockito.when(valueOps.get("refresh:1")).thenReturn(newToken);

        // When & Then
        Assertions.assertThatThrownBy(() -> refreshTokenService.rotate(oldToken))
                .isInstanceOf(InvalidRefreshTokenException.class);

        // Redis 삭제도 호출됐는지 검증
        Mockito.verify(redisTemplate).delete("refresh:1");
    }

    @Test
    @DisplayName("save 호출 시 Redis에 올바른 키와 값으로 저장되어야 한다.")
    public void Save_테스트() {
        // Given
        Long userId = 1L;
        String refreshToken = "token";

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // When
        refreshTokenService.save(userId, refreshToken);

        // Then
        Mockito.verify(valueOps).set("refresh:1", refreshToken, Duration.ofDays(7));
    }

    @Test
    @DisplayName("delete 호출 시 Redis에서 해당 키가 삭제되어야 한다.")
    public void Delete_테스트() {
        // Given
        Long userId = 1L;

        // When
        refreshTokenService.delete(userId);

        // Then
        Mockito.verify(redisTemplate).delete("refresh:1");
    }
}
