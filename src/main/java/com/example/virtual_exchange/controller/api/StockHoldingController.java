package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.config.auth.PrincipalDetails; // ★ import 필수
import com.example.virtual_exchange.dto.ChargeRequestDto;
import com.example.virtual_exchange.dto.MyAssetDto;
import com.example.virtual_exchange.service.StockHoldingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // ★ import 필수
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
public class StockHoldingController {

    private final StockHoldingService stockHoldingService;

    @GetMapping
    public ResponseEntity<MyAssetDto> getMyAssets(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();

        MyAssetDto myAssetDto = stockHoldingService.getMyAssetStatus(userId);

        return ResponseEntity.ok(myAssetDto);
    }

    // 현재는 단순히 임시 돈 추가 API이므로 PATCH 요청으로 설정
    @PatchMapping("/charge")
    public ResponseEntity<String> increaseMoney(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody @Valid ChargeRequestDto dto){
        Long userId = principalDetails.getUser().getId();
        long money = dto.getMoney();
        log.info("[Controller] 원화 충전 요청 - userId: {}, amount: {}", userId, money);

        long finalBalance = stockHoldingService.increaseMoney(userId, money);

        return ResponseEntity.status(HttpStatus.CREATED).body(money + "원이 정상적으로 충전되었습니다. (현재 총 잔고: " + finalBalance + "원)");
    }
}