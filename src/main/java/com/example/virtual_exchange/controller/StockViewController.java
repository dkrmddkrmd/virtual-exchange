package com.example.virtual_exchange.controller;

import com.example.virtual_exchange.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StockViewController {
    private final StockService stockService;

    @GetMapping("/stocks") // 주소창에 localhost:8080/stocks 라고 치면
    public String stockPage(Model model) {
        // 1. 서비스한테 주식 목록 다 가져오라고 시킴
        // 2. 가져온 목록을 "stocks"라는 이름으로 화면(Model)에 실어 보냄
        model.addAttribute("stocks", stockService.getStocks());

        // 3. "stock_list"라는 이름의 HTML 파일을 찾아서 보여줘라!
        return "stock_list";
    }
}
