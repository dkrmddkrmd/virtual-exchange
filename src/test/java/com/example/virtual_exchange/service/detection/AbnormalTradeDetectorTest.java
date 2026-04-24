package com.example.virtual_exchange.service.detection;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.OrderMessageDto;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.StockRepository;
import com.example.virtual_exchange.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

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
    OptionalValu

    @InjectMocks AbnormalTradeDetector abnormalTradeDetector;

    @Test
    @DisplayName("6번 거래되면 거래가 차단된다.")
    public void 다수_매수_테스트(){
        //Given
        User user = new User("test@test.com", "1234", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Account account = new Account(user);
        account.increaseBalance(100000000L);
        Stock stock = new Stock("code", "code", 1000D);

        OrderMessageDto dto = new OrderMessageDto(1L, "code", 1L, "BUY");

        //When
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(account));
        Mockito.when(stockRepository.findById("code")).thenReturn(Optional.of(stock));

        Mockito.when(redisTemplate.opsForZSet())
    }
}
