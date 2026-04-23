package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Order;
import com.example.virtual_exchange.domain.OrderType;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.exception.AbnormalTradeException;
import com.example.virtual_exchange.kafka.producer.StockOrderProducer;
import com.example.virtual_exchange.dto.OrderMessageDto;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.repository.*;
import com.example.virtual_exchange.service.detection.AbnormalTradeDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockOrderProducer stockOrderProducer;
    private final AbnormalTradeDetector detector;

    @Transactional(noRollbackFor = AbnormalTradeException.class)
    public void createOrder(Long userId, OrderRequestDto dto) {
        OrderMessageDto message = new OrderMessageDto(
                userId,
                dto.getCode(),
                dto.getQuantity(),
                dto.getOrderType()
        );

        try {
            detector.checkAbnormalTrade(message);
        } catch (AbnormalTradeException ex) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("없는 유저입니다."));
            Stock stock = stockRepository.findById(dto.getCode())
                    .orElseThrow(() -> new IllegalArgumentException("없는 종목입니다."));

            orderRepository.save(new Order(
                    user,
                    stock,
                    OrderType.valueOf(dto.getOrderType()),
                    stock.getCurrentPrice(),
                    dto.getQuantity(),
                    ex.getMessage()
            ));
            log.warn("[Service] 이상 거래 차단 - User: {}, 사유: {}", userId, ex.getMessage());
            throw ex;
        }

        stockOrderProducer.sendOrder(message);
        log.info("[Service] 주문 요청 전송 완료 - User: {}", userId);
    }

    // 주문 내역 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<OrderHistoryDto> getOrderLists(Long userId, Pageable pageable){
        return orderRepository.findAllByUserIdWithStock(userId, pageable)
                .map(OrderHistoryDto::new);
    }
}