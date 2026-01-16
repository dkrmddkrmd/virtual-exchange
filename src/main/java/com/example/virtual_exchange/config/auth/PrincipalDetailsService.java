package com.example.virtual_exchange.config.auth;

import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("유저 없음: " + username));

        // [변경 전] 스프링 기본 User 객체 반환
        // return org.springframework.security.core.userdetails.User.builder()...build();

        // [변경 후] 우리가 만든 PrincipalDetails 반환 (여기에 user 정보가 다 들어감)
        return new PrincipalDetails(user);
    }
}