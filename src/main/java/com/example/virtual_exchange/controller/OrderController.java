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
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public ResponseEntity<String> buyStock(@RequestBody OrderRequestDto requestDto) {
        // 서비스 호출 (DTO에서 데이터를 꺼내서 전달)
        orderService.buy(requestDto.getUserId(), requestDto.getCode(), requestDto.getQuantity());

        return ResponseEntity.ok("매수 주문 성공!");
    }

    @PostMapping("/sell")
    public ResponseEntity<String> sellStock(@RequestBody OrderRequestDto requestDto) {
        // 서비스 호출 (DTO에서 데이터를 꺼내서 전달)
        orderService.sell(requestDto.getUserId(), requestDto.getCode(), requestDto.getQuantity());

        return ResponseEntity.ok("매도 주문 성공!");
    }
}