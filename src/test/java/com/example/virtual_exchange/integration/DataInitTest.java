package com.example.virtual_exchange.integration;

import com.example.virtual_exchange.service.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("데이터 초기화용 - 필요할 때만 수동 실행")
@SpringBootTest
@ActiveProfiles("local")
public class DataInitTest {
    @Autowired
    UserService userService;

    @Test
    void insertRealUser() {
        userService.join("test@test.com", "tester", "1234");
        System.out.println("✅ 유저 생성 완료!");
    }
}
