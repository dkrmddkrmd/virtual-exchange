package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Order;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.awt.print.Pageable;
import java.util.List;

import static com.example.virtual_exchange.domain.OrderType.BUY;
import static com.example.virtual_exchange.domain.OrderType.SELL;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    OrderRepository orderRepository;
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
}
