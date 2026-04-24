package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.config.auth.JwtUtil;
import com.example.virtual_exchange.config.auth.PrincipalDetails;
import com.example.virtual_exchange.config.auth.PrincipalDetailsService;
import com.example.virtual_exchange.config.auth.SecurityConfig;
import com.example.virtual_exchange.service.RefreshTokenService;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.ChargeRequestDto;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockHoldingController.class)
@TestPropertySource(properties = {"jwt.secret=c3ByaW5nLWJvb3Qtc2VjdXJpdHktand0LXR1dG9yaWFsLXNlY3JldC1rZXktc3ByaW5nLWJvb3Qtc2VjdXJpdHktand0LXR1dG9yaWFsLXNlY3JldC1rZXkK"})
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({SecurityConfig.class, JwtUtil.class})
public class StockHoldingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtUtil jwtUtil;

    @MockitoBean
    StockHoldingService stockHoldingService;

    @MockitoBean
    PrincipalDetailsService principalDetailsService;

    @MockitoBean
    RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("포트폴리오 조회 및 문서 테스트")
    public void 보유_자산_조회_및_문서_테스트() throws Exception {
        //Given
        User user = new User("stockHoldingApiTester@test.com", "1234", "tester");
        Stock stock = new Stock("Code", "Name", 2000D);
        PrincipalDetails principalDetails = new PrincipalDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

        String testJwtToken = jwtUtil.createAccessToken(authentication);

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
        Mockito.when(principalDetailsService.loadUserByUsername(Mockito.anyString())).thenReturn(principalDetails);


        //When & Then
        mockMvc.perform(get("/api/assets")
                        .header("Authorization", "Bearer " + testJwtToken))
                .andExpect(status().isOk())
                .andDo(document("assets",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 인증 토큰 (Bearer 타입 필수)")
                        ),
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

    @Test
    @DisplayName("정상적인 금액(예: 10,000원) 요청 시 201 Created 상태 코드 및 올바른 응답 메시지 반환 검증")
    public void 충전_성공_테스트() throws Exception {
        //Given
        long chargeAmount = 10000L;
        long finalBalance = 60000L;

        User user = new User("chargeTest@test.com", "1234", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        String testJwtToken = jwtUtil.createAccessToken(authentication);

        ChargeRequestDto dto = new ChargeRequestDto(chargeAmount);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        Mockito.when(stockHoldingService.increaseMoney(user.getId(), chargeAmount)).thenReturn(finalBalance);
        Mockito.when(principalDetailsService.loadUserByUsername(Mockito.anyString())).thenReturn(principalDetails);


        //When & Then
        mockMvc.perform(patch("/api/assets/charge")
                .header("Authorization", "Bearer " + testJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string(chargeAmount + "원이 정상적으로 충전되었습니다. (현재 총 잔고: " + finalBalance + "원)"))
                .andDo(document("charge-money",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 인증 토큰 (Bearer 타입 필수)")
                        ),
                        requestFields(
                                fieldWithPath("money").description("충전할 금액 (최소 1원 이상)")
                        )
                ));
    }

    @Test
    @DisplayName("0원 또는 음수 금액 요청 시 @Valid 작동 여부 및 400 Bad Request 예외 반환 검증")
    public void 충전_실패_테스트() throws Exception {
        //Given
        long chargeAmount = -10000L;

        User user = new User("chargeTest@test.com", "1234", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        String testJwtToken = jwtUtil.createAccessToken(authentication);

        ChargeRequestDto dto = new ChargeRequestDto(chargeAmount);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        Mockito.when(principalDetailsService.loadUserByUsername(Mockito.anyString())).thenReturn(principalDetails);


        //When & Then
        mockMvc.perform(patch("/api/assets/charge")
                        .header("Authorization", "Bearer " + testJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("최소 1원 이상 요청해야 합니다."));
    }

    @Test
    @DisplayName("JWT 토큰 없이 요청 시 401 Unauthorized 반환 검증")
    public void 충전_인증_실패_테스트() throws Exception {
        // Given
        ChargeRequestDto dto = new ChargeRequestDto(10000L); // 정상 금액을 넣어도
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        // When & Then
        mockMvc.perform(patch("/api/assets/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }
}