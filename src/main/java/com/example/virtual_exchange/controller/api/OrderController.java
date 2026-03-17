package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.config.auth.PrincipalDetails;
import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.facade.RedissonLockStockFacade;
import com.example.virtual_exchange.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 2. 어노테이션 import
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder(
            @AuthenticationPrincipal PrincipalDetails principalDetails, // 1. 인증 정보 받기
            @RequestBody @Valid OrderRequestDto requestDto) {

        Long userId = principalDetails.getUser().getId();

        orderService.createOrder(userId, requestDto);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("주문 접수 완료");
    }

    // 주문 목록 조회 (페이징 추가)
    // 요청 예시: /api/orders/list?page=0&size=10 (0번 페이지, 10개씩)
    @GetMapping // URL 유지
    public ResponseEntity<Page<OrderHistoryDto>> getOrderLists(
            @AuthenticationPrincipal PrincipalDetails principalDetails,

            // [추가] 페이징 처리 자동 매핑 객체
            // @PageableDefault(size = 10): 안 보내면 기본 10개씩 가져옴
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Long userId = principalDetails.getUser().getId();

        // 서비스에 pageable 전달
        Page<OrderHistoryDto> orderList = orderService.getOrderLists(userId, pageable);

        return ResponseEntity.ok(orderList);
    }
}