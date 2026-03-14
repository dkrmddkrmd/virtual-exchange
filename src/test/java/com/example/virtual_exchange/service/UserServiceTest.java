package com.example.virtual_exchange.service;

import com.example.virtual_exchange.exception.DuplicateEmailException;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock AccountRepository accountRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    @Test
    public void 메일_중복_예외_테스트(){
        //Given
        String email = "test@test.com";
        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);

        //when & then
        Assertions.assertThrows(DuplicateEmailException.class, () -> userService.join(email, "test", "1234"));

        Mockito.verify(userRepository, Mockito.times(1)).existsByEmail(email);
    }
}
