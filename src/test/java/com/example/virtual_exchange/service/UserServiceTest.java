package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.exception.DuplicateEmailException;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Test
    public void 회원가입_성공_테스트(){
        //Given
        Mockito.when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("1234")).thenReturn("5678");

        //When
        Long userId = userService.join("test@test.com", "test", "1234");

        //Then
        Mockito.verify(userRepository).save(Mockito.any());
        Mockito.verify(accountRepository).save(Mockito.any());
        Mockito.verify(passwordEncoder).encode("1234");
    }
}
