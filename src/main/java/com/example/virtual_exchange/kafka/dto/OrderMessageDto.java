package com.example.virtual_exchange.kafka.dto;

import com.example.virtual_exchange.domain.OrderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessageDto {
    private Long userId;
    private String code;
    private Long quantity;
    private String orderType;
}
