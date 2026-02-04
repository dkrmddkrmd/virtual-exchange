package com.example.virtual_exchange.service;

import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.kafka.producer.StockOrderProducer;
import com.example.virtual_exchange.kafka.dto.OrderMessageDto;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
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

        log.info("[Service] 주문 요청 접수 완료 - User: {}, Code: {}", userId, dto.getCode());
    }

    // [변경] 페이징 적용 (List -> Page)
    // Pageable 객체(페이지 번호, 사이즈)를 받아서 처리
    public Page<OrderHistoryDto> getOrderLists(Long userId, Pageable pageable){

        // map() 함수를 쓰면 Page<Order> -> Page<OrderHistoryDto>로 변환도 알아서 해줌!
        return orderRepository.findAllByUserIdWithStock(userId, pageable)
                .map(OrderHistoryDto::new);
    }
}