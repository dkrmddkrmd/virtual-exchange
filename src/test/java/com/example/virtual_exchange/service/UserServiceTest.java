package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    UserService userService;

    @Test
    void JoinTest(){
        //given
        String email = "test@test.com";

        //when
        Long userId = userService.join(email, "test", "1234");

        //then
        User foundUser = userRepository.findById(userId).get();
        assertThat(foundUser.getEmail()).isEqualTo(email);
        assertThat(foundUser.getName()).isEqualTo("test");

        //then 2
        Account foundAccount = accountRepository.findAll().stream()
                .filter(a -> a.getUser().getId().equals(userId))
                .findFirst()
                .get();

        assertThat(foundAccount).isNotNull();
        assertThat(foundAccount.getBalance()).isEqualTo(0L);
    }

    @Test
    void DuplicateJoinTest(){

    }
}