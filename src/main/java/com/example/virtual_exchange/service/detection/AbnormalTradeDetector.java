package com.example.virtual_exchange.service.detection;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.dto.OrderMessageDto;
import com.example.virtual_exchange.exception.AbnormalTradeException;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.StockRepository;
import com.example.virtual_exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AbnormalTradeDetector {
    private final StringRedisTemplate redisTemplate;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SlackNotificationService slackNotificationService;
    private final Clock clock;

    private static final String ORDER_COUNT_PREFIX = "order:count:";
    private static final int MAX_ORDER_PER_MINUTE = 5; // 1분에 5건 초과 시 차단

    // 단일 과다 주문
    public void checkOverTrade(Long userId) {
        String key = ORDER_COUNT_PREFIX + userId;
        long now = System.currentTimeMillis();
        long windowStart = now - 60_000; // 60초 전

        // 1. 60초 이전 데이터 삭제
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // 2. 현재 윈도우 내 주문 수 조회
        Long count = redisTemplate.opsForZSet().size(key);

        // 3. 5건 이상이면 차단
        if (count != null && count >= MAX_ORDER_PER_MINUTE) {
            slackNotificationService.sendAbnormalTradeBlocked(userId, "1분 내 " + MAX_ORDER_PER_MINUTE + "회 초과 주문");
            throw new AbnormalTradeException("단시간 과다 주문으로 차단");
        }

        // 4. 현재 주문 timestamp 추가
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);

        // 5. 키 TTL 갱신 (60초)
        redisTemplate.expire(key, 60, java.util.concurrent.TimeUnit.SECONDS);
    }

    public void checkHeavyTrade(Account account, long totalPrice, String email) {
        if (account.getBalance() * 0.8 <= totalPrice) {
            String reason = "보유 자산의 80% 이상의 거래가 발생했습니다.";
            emailService.sendAbnormalTradeAlert(email, reason);
            slackNotificationService.sendAbnormalTradeWarning(email, reason);
        }
    }

    public void checkLargeTrade(long totalPrice, String email) {
        if (totalPrice >= 10_000_000) {
            String reason = "1천만원 이상의 거래가 발생했습니다.";
            emailService.sendAbnormalTradeAlert(email, reason);
            slackNotificationService.sendAbnormalTradeWarning(email, reason);
        }
    }

    public void checkNightTrade(Account account, long totalPrice, String email) {
        int hour = LocalTime.now(clock).getHour();

        // 새벽 1~5시 아니면 return
        if (hour < 1 || hour >= 5) return;

        // 잔고 50% 이상이면 알림
        if (totalPrice < account.getBalance() * 0.5) return;

        String reason = "심야 시간대(" + hour + "시)에 잔고 50% 이상의 거래가 감지되었습니다.";
        emailService.sendAbnormalTradeAlert(email, reason);
        slackNotificationService.sendAbnormalTradeWarning(email, reason);
    }

    public void checkAbnormalTrade(OrderMessageDto dto){
        Long userId = dto.getUserId();
        String email = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("없는 유저입니다."))
                .getEmail();
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("계좌가 없습니다."));
        long totalPrice = (long) (stockRepository.findById(dto.getCode())
                .orElseThrow(() -> new IllegalArgumentException("없는 종목입니다."))
                .getCurrentPrice() * dto.getQuantity());

        checkOverTrade(userId);
        checkLargeTrade(totalPrice, email);

        if(Objects.equals(dto.getOrderType(), "BUY")){
            checkHeavyTrade(account, totalPrice, email);
            checkNightTrade(account, totalPrice, email);
        }
    }
}
