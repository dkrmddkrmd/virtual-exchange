package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.config.PrincipalDetails;
import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.service.OrderService;
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
    // (참고: 여기도 나중에는 principalDetails를 써서 '누가' 주문했는지 세션에서 가져오는 게 안전합니다.)
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequestDto requestDto) {
        orderService.createOrder(requestDto);
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