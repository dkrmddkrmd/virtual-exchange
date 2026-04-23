package com.example.virtual_exchange.service.detection;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendAlert(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    @Async
    public void sendAbnormalTradeAlert(String to, String tradeInfo) {
        sendAlert(
                to,
                "[가상 거래소] 이상 거래 감지 알림",
                "고액 거래가 감지되었습니다.\n\n" +
                        "거래 정보: " + tradeInfo + "\n\n" +
                        "본인이 요청한 거래가 아니라면 즉시 비밀번호를 변경해주세요."
        );
    }
}
