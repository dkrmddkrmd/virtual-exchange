package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.annotation.LogActivity;
import com.example.virtual_exchange.config.auth.PrincipalDetails;
import com.example.virtual_exchange.domain.ActivityType;
import com.example.virtual_exchange.dto.CursorResult;
import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.facade.RedissonLockStockFacade;
import com.example.virtual_exchange.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @LogActivity(activityType = ActivityType.ORDER_CREATE, description = "주문 생성")
    @PostMapping
    public ResponseEntity<String> createOrder(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid OrderRequestDto requestDto) {

        Long userId = principalDetails.getUser().getId();
        orderService.createOrder(userId, requestDto);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("주문 접수 완료");
    }

    @LogActivity(activityType = ActivityType.ORDER_VIEW, description = "주문 내역 커서 페이징 조회")
    @GetMapping
    public ResponseEntity<CursorResult<OrderHistoryDto>> getOrderLists(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(required = false) Long lastOrderId, // 커서 값 (첫 요청 시엔 안 보내므로 null)
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = principalDetails.getUser().getId();

        log.info("[Controller] 주문 내역 Cursor 페이징 요청 - User: {}, LastOrderId: {}", userId, lastOrderId);

        CursorResult<OrderHistoryDto> orderResult = orderService.getOrderListsCursor(userId, lastOrderId, size);

        return ResponseEntity.ok(orderResult);
    }
}
