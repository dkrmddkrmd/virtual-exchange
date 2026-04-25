package com.example.virtual_exchange.service.detection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class SlackNotificationService {

    private final WebClient webClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SlackNotificationService(@Value("${slack.webhook.url}") String webhookUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(webhookUrl)
                .build();
    }

    @Async
    public void sendAbnormalTradeBlocked(Long userId, String reason) {
        String time = LocalDateTime.now().format(FORMATTER);
        send(String.format(
                "*[🚨 이상 거래 차단]*\n> *시간:* %s\n> *UserID:* `%d`\n> *사유:* %s",
                time, userId, reason
        ));
    }

    @Async
    public void sendAbnormalTradeWarning(String email, String reason) {
        String time = LocalDateTime.now().format(FORMATTER);
        send(String.format(
                "*[⚠️ 이상 거래 감지]*\n> *시간:* %s\n> *대상:* %s\n> *사유:* %s",
                time, email, reason
        ));
    }

    @Async
    public void sendServerError(String exceptionType, String errorMessage) {
        String time = LocalDateTime.now().format(FORMATTER);
        send(String.format(
                "*[💥 서버 오류 발생]*\n> *시간:* %s\n> *예외:* `%s`\n> *메시지:* %s",
                time, exceptionType, errorMessage
        ));
    }

    private void send(String text) {
        try {
            webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("text", text))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.warn("Slack 알림 전송 실패: {}", e.getMessage());
        }
    }
}
