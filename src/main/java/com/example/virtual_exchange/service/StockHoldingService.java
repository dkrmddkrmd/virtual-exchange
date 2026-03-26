package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.Account;
import com.example.virtual_exchange.domain.StockHolding;
import com.example.virtual_exchange.dto.MyAssetDto;
import com.example.virtual_exchange.dto.StockHoldingDto;
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

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("계좌 없음"));
        Long balance = account.getBalance();

        List<StockHolding> myStocks = stockHoldingRepository.findAllByUserId(userId);

        long totalPurchaseAmount = 0;   // 총 매수 금액
        long totalEvaluationAmount = 0; // 총 평가 금액

        for (StockHolding holding : myStocks) {
            Double currentPrice = holding.getStock().getCurrentPrice();
            Double avgPrice = holding.getAvgPrice();
            Long quantity = holding.getQuantity();

            totalPurchaseAmount += (long) (quantity * avgPrice);
            totalEvaluationAmount += (long) (quantity * currentPrice);
        }

        long totalAssetAmount = balance + totalEvaluationAmount;
        long totalProfitLoss = totalEvaluationAmount - totalPurchaseAmount;
        double returnRate = 0.0;

        if (totalPurchaseAmount > 0) {
            returnRate = ((double) totalProfitLoss / totalPurchaseAmount) * 100;
        }

        List<StockHoldingDto> holdingDtos = myStocks.stream()
                .filter(holding -> holding.getQuantity() > 0)
                .map(StockHoldingDto::new) // 생성자 참조를 통해 변환
                .toList();

        return MyAssetDto.builder()
                .totalAssetAmount(totalAssetAmount)
                .balance(balance)
                .totalPurchaseAmount(totalPurchaseAmount)
                .totalEvaluationAmount(totalEvaluationAmount)
                .totalProfitLoss(totalProfitLoss)
                .returnRate(returnRate)
                .holdingList(holdingDtos)
                .build();
    }

    @Transactional
    public long increaseMoney(Long userId, Long money){
        if(money < 0){
            throw new IllegalArgumentException("음수의 돈이 입금되었습니다.");
        }

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("계좌가 존재하지 않음"));

        account.increaseBalance(money);

        return account.getBalance();
    }
}