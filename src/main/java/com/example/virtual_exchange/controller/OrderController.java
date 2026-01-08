package com.example.virtual_exchange.controller;

import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequestDto requestDto) {
        orderService.createOrder(requestDto);
        return ResponseEntity.ok("주문 접수 완료");
    }

    // 주문 목록 조회
    // 1. 데이터를 가져오는 것이니 PostMapping보다 GetMapping이 적절합니다.
    // 2. 리턴 타입은 HTML 주소가 아니라, 실제 데이터 리스트가 되어야 합니다.
    @GetMapping("/list")
    public ResponseEntity<List<OrderHistoryDto>> getOrderLists(@RequestParam Long userId){

        // 서비스에서 주문 목록(List)을 받아옵니다.
        List<OrderHistoryDto> orderList = orderService.getOrderLists(userId);

        // 프론트엔드에게 JSON 데이터로 응답합니다.
        return ResponseEntity.ok(orderList);
    }
}