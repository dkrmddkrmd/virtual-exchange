package com.example.virtual_exchange.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StockHoldingViewController {
    @GetMapping("/my-assets")
    public String myAssetPage() {
        // 데이터는 HTML 내부의 자바스크립트(fetch)가 가져오므로
        // 여기서는 껍데기(HTML 파일)만 리턴하면 됩니다.
        return "my_asset";
    }
}
