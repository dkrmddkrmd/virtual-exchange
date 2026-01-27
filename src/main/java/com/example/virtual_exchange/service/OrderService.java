package com.example.virtual_exchange.service;

import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.kafka.producer.StockOrderProducer;
import com.example.virtual_exchange.kafka.dto.OrderMessageDto;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockOrderProducer stockOrderProducer;

    // [New] 외부 유입 창구
    public void createOrder(Long userId, OrderRequestDto dto) {
        // 1. DTO 변환 (String -> String 그대로 전달)
        OrderMessageDto message = new OrderMessageDto(
                userId,
                dto.getCode(),
                dto.getQuantity(), // RequestDto가 수량도 String이라면 파싱 필요
                dto.getOrderType() // String 그대로
        );

        // 2. 주문 전송 (메서드도 sendOrder로 명확하게)
        stockOrderProducer.sendOrder(message);

        System.out.println("Service: 주문 요청함 -> " + message);
    }

    public List<OrderHistoryDto> getOrderLists(Long userId){

        return orderRepository.findAllByUserIdWithStock(userId).stream()
                .map(OrderHistoryDto::new)
                .toList();
    }

}