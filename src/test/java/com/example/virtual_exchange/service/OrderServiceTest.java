package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Order;
import com.example.virtual_exchange.domain.OrderStatus;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.exception.AbnormalTradeException;
import com.example.virtual_exchange.kafka.producer.StockOrderProducer;
import com.example.virtual_exchange.repository.OrderRepository;
import com.example.virtual_exchange.repository.StockRepository;
import com.example.virtual_exchange.repository.UserRepository;
import com.example.virtual_exchange.service.detection.AbnormalTradeDetector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.example.virtual_exchange.domain.OrderType.BUY;
import static com.example.virtual_exchange.domain.OrderType.SELL;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    OrderRepository orderRepository;
    @Mock
    StockOrderProducer stockOrderProducer;
    @Mock
    AbnormalTradeDetector detector;
    @Mock
    UserRepository userRepository;
    @Mock
    StockRepository stockRepository;
    @InjectMocks
    OrderService orderService;

    @Test
    @DisplayName("주문 내역 조회 시 잘 작동한다.")
    public void 주문_내역_조회_테스트(){
        //Given
        User user = new User("test@test.com", "1234", "tester");
        PageRequest pageRequest = PageRequest.of(0, 10);
        Stock stock1 = new Stock("Code", "name", 1000D);
        Stock stock2 = new Stock("Code2", "name2", 10000D);
        Order order1 = new Order(user, stock1, SELL, 900D, 2L);
        Order order2 = new Order(user, stock2, BUY, 9000D, 1L);
        List<Order> orderList = List.of(order1, order2);
        Page<Order> orderPage = new PageImpl<>(orderList);

        Mockito.when(orderRepository.findAllByUserIdWithStock(user.getId(), pageRequest)).thenReturn(orderPage);

        //When
        Page<OrderHistoryDto> page = orderService.getOrderLists(user.getId(), pageRequest);

        //Then
        Assertions.assertEquals(2, page.getTotalElements());
        Assertions.assertEquals(900D, page.getContent().get(0).getPrice());
    }

    @Test
    public void 주문_성공_테스트() {
        // Given
        Long userId = 1L;
        OrderRequestDto dto = new OrderRequestDto("CODE", 1L, "BUY");

        Mockito.doNothing().when(detector).checkAbnormalTrade(Mockito.any());

        // When
        orderService.createOrder(userId, dto);

        // Then
        Mockito.verify(stockOrderProducer).sendOrder(Mockito.any());
    }

    @Test
    @DisplayName("이상 거래 감지 시 FAILED Order가 저장되고 예외가 재전파된다.")
    public void 이상_거래_차단_테스트() {
        // Given
        Long userId = 1L;
        OrderRequestDto dto = new OrderRequestDto("CODE", 1L, "BUY");

        User user = new User("test@test.com", "1234", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Stock stock = new Stock("CODE", "코인", 1000D);

        Mockito.doThrow(new AbnormalTradeException("단시간 과다 주문으로 차단"))
                .when(detector).checkAbnormalTrade(Mockito.any());

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(stockRepository.findById("CODE")).thenReturn(Optional.of(stock));

        // When & Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> orderService.createOrder(userId, dto))
                .isInstanceOf(AbnormalTradeException.class);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        Mockito.verify(orderRepository).save(captor.capture());
        Assertions.assertEquals(OrderStatus.FAILED, captor.getValue().getStatus());
        Assertions.assertEquals("단시간 과다 주문으로 차단", captor.getValue().getFailReason());
    }
}
