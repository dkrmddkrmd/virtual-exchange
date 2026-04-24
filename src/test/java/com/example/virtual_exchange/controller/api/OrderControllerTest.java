package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.config.auth.JwtUtil;
import com.example.virtual_exchange.config.auth.PrincipalDetails;
import com.example.virtual_exchange.config.auth.PrincipalDetailsService;
import com.example.virtual_exchange.config.auth.SecurityConfig;
import com.example.virtual_exchange.domain.Order;
import com.example.virtual_exchange.domain.OrderType;
import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.OrderHistoryDto;
import com.example.virtual_exchange.dto.OrderRequestDto;
import com.example.virtual_exchange.exception.AbnormalTradeException;
import com.example.virtual_exchange.service.OrderService;
import com.example.virtual_exchange.service.RefreshTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({SecurityConfig.class, JwtUtil.class})
@TestPropertySource(properties = {"jwt.secret=c3ByaW5nLWJvb3Qtc2VjdXJpdHktand0LXR1dG9yaWFsLXNlY3JldC1rZXktc3ByaW5nLWJvb3Qtc2VjdXJpdHktand0LXR1dG9yaWFsLXNlY3JldC1rZXkK"})
public class OrderControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    OrderService orderService;
    @MockitoBean
    RefreshTokenService refreshTokenService;
    @Autowired
    JwtUtil jwtUtil;
    @MockitoBean
    PrincipalDetailsService principalDetailsService;

    @Test
    @DisplayName("주문 로직 및 문서화 테스트")
    public void 주문_문서화_테스트() throws Exception {
        //Given
        User user = new User("orderApiTest@test.comn", "1234", "teseter");
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        String testJwtToken = jwtUtil.createAccessToken(authentication);

        OrderRequestDto dto = new OrderRequestDto("Code", 1L, "BUY");

        String json = objectMapper.writeValueAsString(dto);

        Mockito.when(principalDetailsService.loadUserByUsername(Mockito.anyString())).thenReturn(principalDetails);

        //When & Then
        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + testJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isAccepted())
                .andDo(document("create-order",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 인증 토큰 (Bearer 타입 필수)")
                        ),
                        requestFields(
                                fieldWithPath("code").description("주식 종목 코드"),
                                fieldWithPath("quantity").description("주문 수량 (최소 1주 이상)"),
                                fieldWithPath("orderType").description("주문 타입 (BUY 또는 SELL)")
                        )
                        ));
    }

    @Test
    @DisplayName("주문 내역 로직 및 문서화 테스트")
    public void 주문_내역_문서화_테스트() throws Exception {
        //Given
        User user = new User("orderListApiTest@test.comn", "1234", "teseter");
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        String testJwtToken = jwtUtil.createAccessToken(authentication);

        Stock stock = new Stock("Code", "Name", 1000D);

        PageRequest pageRequest = PageRequest.of(0, 10);

        OrderHistoryDto dto = new OrderHistoryDto(1L, "Code", "매수", 1L, 1500D, 1500D, "2026-03-19 11:15", "SUCCESS", null);
        List<OrderHistoryDto> orderList = List.of(dto);
        Page<OrderHistoryDto> orderPage = new PageImpl<>(orderList);


        Mockito.when(orderService.getOrderLists(Mockito.any(), Mockito.any())).thenReturn(orderPage);
        Mockito.when(principalDetailsService.loadUserByUsername(Mockito.anyString())).thenReturn(principalDetails);

        //When % Then
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + testJwtToken))
                .andExpect(status().isOk())
                .andDo(document("get-order-list",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 인증 토큰 (Bearer 타입 필수)")
                        ),
                        //명시한 핵심 필드만 문서화해 주는 도구
                        relaxedResponseFields(
                                //실제 데이터는 "content" 배열 안에 있으므로 content[]. 로 시작
                                fieldWithPath("content[].orderId").description("주문 ID"),
                                fieldWithPath("content[].stockName").description("주식 이름"),
                                fieldWithPath("content[].orderType").description("주문 타입 (매수/매도)"),
                                fieldWithPath("content[].quantity").description("주문 수량"),
                                fieldWithPath("content[].price").description("1주당 가격"),
                                fieldWithPath("content[].totalAmount").description("총 거래 금액"),
                                fieldWithPath("content[].orderDate").description("주문 일시 (yyyy-MM-dd HH:mm)"),

                                //프론트엔드가 페이징 바(1, 2, 3...)를 그릴 때 필요한 핵심 정보만 추가
                                fieldWithPath("page.totalElements").description("전체 데이터 개수"),
                                fieldWithPath("page.totalPages").description("전체 페이지 수"),
                                fieldWithPath("page.number").description("현재 페이지 번호 (0부터 시작)"),
                                fieldWithPath("page.size").description("페이지당 데이터 개수")
                        )
                ));
    }

    @Test
    @DisplayName("이상 거래시 403이 나오는지")
    public void 이상_거래_테스트() throws Exception {
        //Given
        User user = new User("test@test.com", "1234", "tester");
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        String testJwtToken = jwtUtil.createAccessToken(authentication);

        OrderRequestDto dto = new OrderRequestDto("Code", 1L, "BUY");
        String json = objectMapper.writeValueAsString(dto);

        Mockito.when(principalDetailsService.loadUserByUsername(Mockito.anyString()))
                .thenReturn(principalDetails);

        // AbnormalTradeException 던지도록 설정
        Mockito.doThrow(new AbnormalTradeException("단시간 과다 주문으로 차단"))
                .when(orderService).createOrder(Mockito.any(), Mockito.any());

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + testJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andDo(document("order-abnormal-trade",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 인증 토큰 (Bearer 타입 필수)")
                        ),
                        requestFields(
                                fieldWithPath("code").description("주식 종목 코드"),
                                fieldWithPath("quantity").description("주문 수량"),
                                fieldWithPath("orderType").description("주문 타입 (BUY 또는 SELL)")
                        ),
                        responseFields(
                                fieldWithPath("code").description("에러 코드 (T001)"),
                                fieldWithPath("message").description("차단 사유")
                        )
                ));
    }
}
