package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.annotation.LogActivity;
import com.example.virtual_exchange.config.auth.CookieUtil;
import com.example.virtual_exchange.domain.ActivityType;
import com.example.virtual_exchange.dto.TokenInfo;
import com.example.virtual_exchange.dto.UserSignupDto;
import com.example.virtual_exchange.exception.InvalidRefreshTokenException;
import com.example.virtual_exchange.service.RefreshTokenService;
import com.example.virtual_exchange.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @LogActivity(activityType = ActivityType.SIGNUP, description = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid UserSignupDto dto) {
        userService.join(dto.getEmail(), dto.getName(), dto.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공!");
    }

    @LogActivity(activityType = ActivityType.LOGOUT, description = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) throw new InvalidRefreshTokenException();
        Long userId = refreshTokenService.extractUserId(refreshToken);
        refreshTokenService.delete(userId);
        CookieUtil.clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    @LogActivity(activityType = ActivityType.TOKEN_REISSUE, description = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<TokenInfo> reissue(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) throw new InvalidRefreshTokenException();
        RefreshTokenService.ReissueResult result = refreshTokenService.rotate(refreshToken);
        CookieUtil.setRefreshCookie(response, result.refreshToken());
        return ResponseEntity.ok(new TokenInfo("Bearer", result.accessToken()));
    }
}
