package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정 (성능 최적화)
@RequiredArgsConstructor        // Lombok: final 붙은 필드의 생성자를 자동으로 만들어줌 (직접 짠 생성자와 똑같음!)
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    // 1. 회원가입
    @Transactional // 여기는 데이터를 쓰니까 readOnly = false (기본값)
    public Long join(String email, String name, String password) {

        // A. 중복 검사: 이미 있는 이메일인지 확인
        // (Optional을 사용해서 값이 있는지 확인)
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

        // B. 회원 저장: 입력받은 정보로 User 객체 생성 후 저장
        User user = new User(email, password, name);
        userRepository.save(user); // 이때 DB에 저장되고 ID가 생김

        // C. 계좌 생성: 가입한 유저의 계좌를 자동 생성 (초기 잔액 0원)
        Account account = new Account(user);
        accountRepository.save(account);

        return user.getId();
    }

    // 2. 로그인
    public User login(String email, String password) {
        // A. 이메일로 회원 조회 (없으면 에러)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // B. 비밀번호 일치 확인 (다르면 에러)
        // (실무에선 암호화된 비번을 비교해야 하지만, 지금은 문자열 비교)
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user; // 로그인 성공 시 유저 객체 반환
    }
}