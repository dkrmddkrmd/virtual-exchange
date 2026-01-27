package com.example.virtual_exchange.config.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // 추가됨
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// AntPathRequestMatcher 임포트 없어도 됨!

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 개발 단계에서는 CSRF 잠깐 꺼두기 (이거 때문에 403 에러 많이 남)
                //.csrf(AbstractHttpConfigurer::disable)
                .csrf(csrf -> csrf.disable()) // jmeter 테스트용
                // 2. URL 별 권한 관리
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 (css, js, images) 무조건 허용! -> 이거 안 하면 로그인 페이지 깨짐
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // 메인화면, 회원가입 등 누구나 봐야 하는 페이지
                        .requestMatchers("/", "/login", "/order_list.html", "/signup", "/api/signup", "/error").permitAll()

                        // 그 외에는 다 로그인 필요
                        .anyRequest().authenticated()
                )

                // 3. 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login") // 커스텀 로그인 페이지 (컨트롤러에 @GetMapping("/") 있어야 함)
                        .usernameParameter("email") // form 태그의 name="email"
                        .defaultSuccessUrl("/") // 성공 시 리다이렉트
                        .permitAll() // 로그인 페이지 접근 권한 열어주기
                )

                // 4. 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/members/logout") // 굳이 Matcher 객체 안 써도 문자열로 가능!
                        .logoutSuccessUrl("/") // 로그아웃 후 메인으로
                        .invalidateHttpSession(true) // 세션 날리기
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt라는 해시 함수를 써서 비밀번호를 암호화하는 기계를 반환
        return new BCryptPasswordEncoder();
    }
}