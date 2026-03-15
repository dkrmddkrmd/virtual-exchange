package com.example.virtual_exchange.controller.view;

import com.example.virtual_exchange.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StockViewController {
    private final StockService stockService;

    @GetMapping("/stocks")
    public String stockPage(Model model) {
        model.addAttribute("stocks", stockService.getAllStocks());
        return "stock_list";
    }
}
