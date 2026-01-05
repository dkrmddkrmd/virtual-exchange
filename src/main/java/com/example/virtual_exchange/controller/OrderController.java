package com.example.virtual_exchange.controller;

import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequestDto requestDto) {

        // Controller는 if/else 로직을 몰라도 됩니다.
        // 그냥 Service의 createOrder한테 "이거 처리해줘"라고 던지면 끝입니다.
        orderService.createOrder(requestDto);

        return ResponseEntity.ok("주문 접수 완료");
    }
}