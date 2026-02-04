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

    // 주문 요청 (Kafka로 메시지 전송)
    public void createOrder(Long userId, OrderRequestDto dto) {
        OrderMessageDto message = new OrderMessageDto(
                userId,
                dto.getCode(),
                dto.getQuantity(),
                dto.getOrderType()
        );

        stockOrderProducer.sendOrder(message);
        log.info("[Service] 주문 요청 전송 완료 - User: {}", userId);
    }

    // 주문 내역 조회 (페이징)
    public Page<OrderHistoryDto> getOrderLists(Long userId, Pageable pageable){
        return orderRepository.findAllByUserIdWithStock(userId, pageable)
                .map(OrderHistoryDto::new);
    }
}