package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.StockHolding;
import com.example.virtual_exchange.dto.MyAssetDto;
import com.example.virtual_exchange.repository.AccountRepository;
import com.example.virtual_exchange.repository.StockHoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockHoldingService {

    private final StockHoldingRepository stockHoldingRepository;
    private final AccountRepository accountRepository;

    public MyAssetDto getMyAssetStatus(Long userId) {

        // 1. [자산] 예수금 가져오기
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("계좌 없음"));
        Long balance = account.getBalance();

        // 2. [주식] 리스트 가져오기
        List<StockHolding> myStocks = stockHoldingRepository.findAllByUserId(userId);

        // 3. [계산] 합산 로직
        long totalPurchaseAmount = 0;   // 총 매수 금액
        long totalEvaluationAmount = 0; // 총 평가 금액

        for (StockHolding holding : myStocks) {
            // ★ 도메인 활용 포인트!
            // holding 안에서 바로 Stock을 꺼내서 가격을 알아냅니다.
            Double currentPrice = holding.getStock().getCurrentPrice();
            Double avgPrice = holding.getAvgPrice();
            Long quantity = holding.getQuantity();

            // 계산 (DTO가 Long이므로 소수점 버림 처리 예시)
            totalPurchaseAmount += (long) (quantity * avgPrice);
            totalEvaluationAmount += (long) (quantity * currentPrice);
        }

        // 4. [최종] 파생 데이터 계산
        long totalAssetAmount = balance + totalEvaluationAmount;
        long totalProfitLoss = totalEvaluationAmount - totalPurchaseAmount;
        double returnRate = 0.0;

        if (totalPurchaseAmount > 0) {
            returnRate = ((double) totalProfitLoss / totalPurchaseAmount) * 100;
        }

        // 5. 반환 (Builder 안 쓰고 생성자로 하는 법)
        // DTO 클래스 위에 @AllArgsConstructor가 있어야 합니다.
        return new MyAssetDto(
                totalAssetAmount,
                balance,
                totalPurchaseAmount,
                totalEvaluationAmount,
                totalProfitLoss,
                returnRate
        );
    }
}