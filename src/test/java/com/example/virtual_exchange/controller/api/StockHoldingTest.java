package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.config.auth.PrincipalDetails;
import com.example.virtual_exchange.controller.api.StockHoldingController;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.StockHolding;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.MyAssetDto;
import com.example.virtual_exchange.dto.StockHoldingDto;
import com.example.virtual_exchange.service.StockHoldingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockHoldingController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class StockHoldingTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    StockHoldingService stockHoldingService;

    @Test
    @DisplayName("포트폴리오 조회 및 문서 테스트")
    public void 보유_자산_조회_및_문서_테스트() throws Exception {
        //Given
        User user = new User("stockHoldingApiTester@test.com", "1234", "tester");
        Stock stock = new Stock("Code", "Name", 2000D);
        PrincipalDetails principalDetails = new PrincipalDetails(user);

        //SecurityContext(VIP룸)에 가짜 신분증을 다이렉트로 꽂아넣습니다!
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        StockHoldingDto holding1 = StockHoldingDto.builder()
                .stockCode("KRW-BTC")
                .stockName("비트코인")
                .quantity(1L)
                .avgPrice(10000D)
                .currentPrice(5000D)
                .profitRate(0.5D)
                .purchaseAmount(10000L)
                .evaluationAmount(5000L)
                .build();

        List<StockHoldingDto> fakeHoldingList = List.of(holding1);

        MyAssetDto fakeAssetDto = MyAssetDto.builder()
                .totalAssetAmount(150000000L)
                .balance(50000000L)
                .totalPurchaseAmount(80000000L)
                .totalEvaluationAmount(100000000L)
                .totalProfitLoss(20000000L)
                .returnRate(25.0)
                .holdingList(fakeHoldingList)
                .build();

        Mockito.when(stockHoldingService.getMyAssetStatus(Mockito.any())).thenReturn(fakeAssetDto);

        //When & Then
        mockMvc.perform(get("/api/my-assets"))
                .andExpect(status().isOk())
                .andDo(document("my-assets",
                        responseFields(
                                fieldWithPath("totalAssetAmount").description("총 자산 금액"),
                                fieldWithPath("balance").description("보유 예수금"),
                                fieldWithPath("totalPurchaseAmount").description("총 매수 금액"),
                                fieldWithPath("totalEvaluationAmount").description("총 평가 금액"),
                                fieldWithPath("totalProfitLoss").description("총 평가 손익"),
                                fieldWithPath("returnRate").description("총 수익률"),

                                fieldWithPath("holdingList").description("보유 주식 목록"),
                                fieldWithPath("holdingList[].stockName").description("주식 이름"),
                                fieldWithPath("holdingList[].stockCode").description("주식 종목 코드"),
                                fieldWithPath("holdingList[].quantity").description("보유 수량"),
                                fieldWithPath("holdingList[].avgPrice").description("매수 평균 단가"),
                                fieldWithPath("holdingList[].currentPrice").description("현재가"),
                                fieldWithPath("holdingList[].profitRate").description("수익률"),
                                fieldWithPath("holdingList[].purchaseAmount").description("총 매수 금액"),
                                fieldWithPath("holdingList[].evaluationAmount").description("총 평가 금액")
                        )
                ));
    }
}