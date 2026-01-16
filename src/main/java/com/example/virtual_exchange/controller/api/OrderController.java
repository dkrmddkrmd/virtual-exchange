package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.config.auth.PrincipalDetails;
import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 2. 어노테이션 import
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    public ResponseEntity<String> createOrder(
            @AuthenticationPrincipal PrincipalDetails principalDetails, // 1. 인증 정보 받기
            @RequestBody @Valid OrderRequestDto requestDto) {

        // 2. 세션에서 로그인한 사람의 ID 꺼내기
        Long userId = principalDetails.getUser().getId();

        // 3. 서비스에 ID와 주문 정보를 따로 넘겨줌 (서비스 메서드 수정 필요!)
        orderService.createOrder(userId, requestDto);

        return ResponseEntity.ok("주문 접수 완료");
    }

    // 주문 목록 조회
    @GetMapping("/list")
    // [변경 전] @RequestParam Long userId
    // [변경 후] @AuthenticationPrincipal PrincipalDetails principalDetails
    public ResponseEntity<List<OrderHistoryDto>> getOrderLists(@AuthenticationPrincipal PrincipalDetails principalDetails){

        // 3. 세션 정보(principalDetails)에서 로그인한 회원의 ID를 꺼냅니다.
        Long userId = principalDetails.getUser().getId();

        // 서비스 호출 (기존과 동일)
        List<OrderHistoryDto> orderList = orderService.getOrderLists(userId);

        return ResponseEntity.ok(orderList);
    }
}