package com.example.virtual_exchange.config;

import com.example.virtual_exchange.domain.User; // 유저 엔티티 경로
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.ArrayList;

public class PrincipalDetails implements UserDetails {

    private final User user; // 우리의 User 엔티티를 품고 있음

    public PrincipalDetails(User user) {
        this.user = user;
    }

    // ★ 핵심: 컨트롤러에서 꺼내 쓸 수 있게 getter 제공
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> user.getRole().name());
        return authorities;
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getEmail(); }

    // 계정 만료/잠김 여부 등 (지금은 무조건 true로 설정)
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}