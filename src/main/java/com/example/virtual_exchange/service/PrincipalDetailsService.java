package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // 1. 스프링 빈으로 등록
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 2. 이 메서드가 핵심입니다. (시큐리티가 로그인 요청 시 자동으로 호출함)
    // 파라미터 username은 프론트엔드에서 입력한 'email'이 들어옵니다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // A. DB에서 유저 조회
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 회원을 찾을 수 없습니다: " + username));

        // B. 조회된 User 엔티티 -> 시큐리티 전용 UserDetails 객체로 변환해서 반환
        // (org.springframework.security.core.userdetails.User 빌더 사용)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())       // 아이디(이메일)
                .password(user.getPassword())    // 암호화된 비밀번호
                .roles(user.getRole().name())    // 권한 (Enum -> String 변환: "USER")
                .build();
    }
}