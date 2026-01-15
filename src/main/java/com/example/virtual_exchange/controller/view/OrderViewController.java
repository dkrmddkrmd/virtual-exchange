package com.example.virtual_exchange.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders") // 공통 주소 (프리픽스)
public class OrderViewController {

    // 거래 내역 페이지 보여주기
    // 실제 주소: /orders/list
    @GetMapping("/list")
    public String orderListPage() {
        return "order_list"; // templates/order_list.html 을 찾아감
    }
}