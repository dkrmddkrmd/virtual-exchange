package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.config.auth.PrincipalDetails; // ★ import 필수
import com.example.virtual_exchange.dto.MyAssetDto;
import com.example.virtual_exchange.service.StockHoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // ★ import 필수
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-assets")
public class StockHoldingController {

    private final StockHoldingService stockHoldingService;

    @GetMapping
    public ResponseEntity<MyAssetDto> getMyAssets(@AuthenticationPrincipal PrincipalDetails principalDetails) {

        // 1. 세션에서 로그인한 사람의 ID를 안전하게 꺼냅니다.
        Long userId = principalDetails.getUser().getId();

        // 2. 서비스에게 "이 사람(userId) 자산 가져와" 라고 시킵니다.
        MyAssetDto myAssetDto = stockHoldingService.getMyAssetStatus(userId);

        return ResponseEntity.ok(myAssetDto);
    }
}