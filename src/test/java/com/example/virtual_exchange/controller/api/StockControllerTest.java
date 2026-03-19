package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.domain.Stock;
import com.example.virtual_exchange.dto.StockResponseDto;
import com.example.virtual_exchange.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class StockControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    StockService stockService;

    @Test
    @DisplayName("주식 리스트 구현 컨트롤러 및 문서 추출")
    public void 주식_리스트_구현_및_문서화_테스트() throws Exception {
        // 1. Given: 바디가 텅 비지 않도록, 가짜 주식 데이터를 하나 만들어서 리스트로 감싸줍니다.
        Stock stock = new Stock("KRW-BTC", "비트코인", 100000000D);
        StockResponseDto fakeStock = new StockResponseDto(stock);
        Mockito.when(stockService.getAllStocks()).thenReturn(List.of(fakeStock)); // 대본 입력 완료!

        //When & Then
        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andDo(document("stock-list",
                        responseFields(
                                fieldWithPath("[].code").description("주식 종목 코드"),
                                fieldWithPath("[].name").description("주식 이름"),
                                fieldWithPath("[].price").description("현재 가격")
                        )
                ));
    }
}
