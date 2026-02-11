package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.exception.DuplicateEmailException;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정 (성능 최적화)
@RequiredArgsConstructor        // Lombok: final 붙은 필드의 생성자를 자동으로 만들어줌 (직접 짠 생성자와 똑같음!)
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 (쓰기 작업이므로 readOnly = false)
    @Transactional
    public Long join(String email, String name, String password) {

        // A. 중복 검사: existsByEmail 사용 추천 (쿼리가 가벼움)
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("이미 가입된 이메일입니다: " + email);
        }

        // B. 비밀번호 암호화 및 유저 저장
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword, name);
        userRepository.save(user);

        // C. 계좌 생성 (초기 잔액 0원)
        // [Tip] Account 생성자 안에서 user와의 연관관계 편의 메서드가 있다면 더 좋음
        Account account = new Account(user);
        accountRepository.save(account);

        return user.getId();
    }
}