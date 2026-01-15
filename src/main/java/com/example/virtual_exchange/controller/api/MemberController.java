package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.dto.MemberSignupDto;
import com.example.virtual_exchange.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final UserService userService;

    @PostMapping("/api/signup")
    public ResponseEntity<String> signup(@RequestBody MemberSignupDto dto) {
        try {
            userService.join(dto.getEmail(), dto.getName(), dto.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공!"); // 201 Created 반환
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}