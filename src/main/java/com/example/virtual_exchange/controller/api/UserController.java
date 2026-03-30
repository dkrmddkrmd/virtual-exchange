package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.dto.UserSignupDto;
import com.example.virtual_exchange.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/signup")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<String> signup(@RequestBody @Valid UserSignupDto dto) {
        userService.join(dto.getEmail(), dto.getName(), dto.getPassword());

        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공!");
    }
}