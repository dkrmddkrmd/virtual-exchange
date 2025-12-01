package com.example.virtual_exchange;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class DomainTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    AccountRepository accountRepository;

    @Test
    @DisplayName("회원가입을 하고 계좌를 생성하면 DB에 잘 들어가야 한다")
    void userAndAccountTest() {
        // --- 1. 회원가입 (User 저장) ---
        // Given: 유저 정보를 만들고
        User user = new User("test@test.com", "1234", "테스트");

        // When: DB에 저장하면
        User savedUser = userRepository.save(user);

        // Then: ID가 생성되어야 하고, 입력한 정보와 같아야 한다.
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@test.com");


        // --- 2. 계좌 개설 (User와 연결) ---
        // Given: 위에서 만든 유저의 계좌를 만들고
        Account account = new Account(savedUser);

        // When: DB에 저장하면
        accountRepository.save(account);

        // Then: 계좌 ID가 생겨야 하고, 잔액은 0원이며, 주인이 맞아야 한다.
        assertThat(account.getId()).isNotNull();
        assertThat(account.getBalance()).isEqualTo(0L);
        assertThat(account.getUser().getName()).isEqualTo("테스트"); // User와 연결 확인!

        System.out.println(">>> 테스트 성공! 생성된 User ID: " + savedUser.getId());
    }
}
