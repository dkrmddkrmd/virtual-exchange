package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.exception.DuplicateEmailException;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long join(String email, String name, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword, name);
        userRepository.save(user);

        Account account = new Account(user);
        accountRepository.save(account);
        log.info("새로운 유저 {}가 계좌{} 와 함께 생성", user.getId(), account.getId());

        return user.getId();
    }
}