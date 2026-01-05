package com.example.virtual_exchange.controller;

import com.example.virtual_exchange.dto.MyAssetDto;
import com.example.virtual_exchange.service.StockHoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-assets")
public class StockHoldingController {

    private final StockHoldingService stockHoldingService;

    // @GetMapping("/") <- 이렇게 하면 주소가 /api/my-assets/ (뒤에 슬래시 포함)가 됩니다.
    // 보통은 깔끔하게 슬래시 없이 씁니다.
    @GetMapping
    public ResponseEntity<MyAssetDto> getMyAssets(@RequestParam Long userId) {

        MyAssetDto myAssetDto = stockHoldingService.getMyAssetStatus(userId);

        // "성공적으로 처리됐어(ok)!" 라는 스티커를 붙여서 데이터를 상자에 담음
        return ResponseEntity.ok(myAssetDto);
    }
}