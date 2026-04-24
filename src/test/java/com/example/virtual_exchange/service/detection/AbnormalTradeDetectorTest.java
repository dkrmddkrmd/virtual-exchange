package com.example.virtual_exchange.service.detection;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.OrderMessageDto;
import com.example.virtual_exchange.exception.AbnormalTradeException;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.StockRepository;
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
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AbnormalTradeDetectorTest {
    @Mock
    StringRedisTemplate redisTemplate;
    @Mock
    AccountRepository accountRepository;
    @Mock
    StockRepository stockRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    EmailService emailService;
    @Mock
    ZSetOperations<String, String> valueOps;
    @Mock
    Clock clock;

    @InjectMocks AbnormalTradeDetector abnormalTradeDetector;

    @Test
    @DisplayName("이상 거래 탐지에서 통과한다.")
    public void 탐지_성공_테스트() {
        // Given
        User user = new User("test@test.com", "1234", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Account account = new Account(user);
        account.increaseBalance(100_000_000L);
        Stock stock = new Stock("code", "code", 1000D);

        OrderMessageDto dto = new OrderMessageDto(1L, "code", 1L, "BUY");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        Mockito.when(stockRepository.findById("code")).thenReturn(Optional.of(stock));
        Mockito.when(redisTemplate.opsForZSet()).thenReturn(valueOps);
        Mockito.when(valueOps.size("order:count:1")).thenReturn(4L);

        Instant noon = LocalDate.now().atTime(12, 0).toInstant(ZoneOffset.UTC);
        Mockito.when(clock.instant()).thenReturn(noon);
        Mockito.when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        // Then
        Assertions.assertThatNoException()
                .isThrownBy(() -> abnormalTradeDetector.checkAbnormalTrade(dto));
    }

    @Test
    @DisplayName("5번 초과로 거래하면 차단된다.")
    public void 다수_거래_테스트() {
        // Given
        Long userId = 1L;

        Mockito.when(redisTemplate.opsForZSet()).thenReturn(valueOps);
        Mockito.when(valueOps.size("order:count:1")).thenReturn(5L);

        // When & Then
        Assertions.assertThatThrownBy(() -> abnormalTradeDetector.checkOverTrade(userId))
                .isInstanceOf(AbnormalTradeException.class);
    }

    @Test
    @DisplayName("잔고 80% 이상 매수 시 이메일이 발송된다.")
    public void checkHeavyTrade_테스트() {
        // Given
        User user = new User("test@test.com", "1234", "tester");
        Account account = new Account(user);
        account.increaseBalance(10_000_000L);
        long totalPrice = 8_000_000L;

        // When
        abnormalTradeDetector.checkHeavyTrade(account, totalPrice, "test@test.com");

        // Then
        Mockito.verify(emailService)
                .sendAbnormalTradeAlert("test@test.com", "보유 자산의 80% 이상의 거래가 발생했습니다.");
    }

    @Test
    @DisplayName("1천만원 이상 거래 시 이메일이 발송된다.")
    public void checkLargeTrade_테스트() {
        // Given
        long totalPrice = 10_000_000L;

        // When
        abnormalTradeDetector.checkLargeTrade(totalPrice, "test@test.com");

        // Then
        Mockito.verify(emailService)
                .sendAbnormalTradeAlert("test@test.com", "1천만원 이상의 거래가 발생했습니다.");
    }

    @Test
    @DisplayName("심야 시간대에 잔고 50% 이상 거래 시 이메일이 발송된다.")
    public void checkNightTrade_테스트() {
        // Given
        User user = new User("test@test.com", "1234", "tester");
        Account account = new Account(user);
        account.increaseBalance(10_000_000L);
        long totalPrice = 5_000_000L;

        Instant twoAm = LocalDate.now().atTime(2, 0).toInstant(ZoneOffset.UTC);
        Mockito.when(clock.instant()).thenReturn(twoAm);
        Mockito.when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        // When
        abnormalTradeDetector.checkNightTrade(account, totalPrice, "test@test.com");

        // Then
        Mockito.verify(emailService).sendAbnormalTradeAlert(
                Mockito.eq("test@test.com"),
                Mockito.contains("심야 시간대")
        );
    }
}
