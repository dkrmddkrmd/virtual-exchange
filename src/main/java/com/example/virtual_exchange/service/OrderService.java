package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Order;
import com.example.virtual_exchange.domain.OrderType;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.CursorResult;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    // 주문 내역 커서 페이징 조회
    @Transactional(readOnly = true)
    public CursorResult<OrderHistoryDto> getOrderListsCursor(Long userId, Long lastOrderId, int size) {

        // 핵심 로직: 클라이언트가 10개를 원하면, DB에는 11개를 요청한다!
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Order> orders;

        // 1. 데이터 조회 (커서 유무 분기)
        if (lastOrderId == null) {
            orders = orderRepository.findFirstPageByUserId(userId, pageable);
        } else {
            orders = orderRepository.findNextPageByUserId(userId, lastOrderId, pageable);
        }

        // 2. 다음 페이지 유무(hasNext) 판별
        boolean hasNext = false;
        if (orders.size() > size) {
            hasNext = true;
            orders.remove(size); // 프론트엔드에게는 요청한 딱 10개만 줘야 하므로 마지막 1개(11번째)는 응답 리스트에서 제거!
        }

        // 3. DTO 변환
        List<OrderHistoryDto> content = orders.stream()
                .map(OrderHistoryDto::new)
                .collect(Collectors.toList());

        // 4. 다음 커서 값 추출 (리스트의 가장 마지막 데이터의 ID)
        // DTO의 ID Getter 이름(예: getId 또는 getOrderId)에 맞게 수정해주세요!
        Long nextCursor = hasNext ? content.get(content.size() - 1).getOrderId() : null;

        return new CursorResult<>(content, nextCursor, hasNext);
    }
}