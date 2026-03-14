package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.dto.UserSignupDto;
import com.example.virtual_exchange.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("비밀번호 4자리 미만일때 유효성 검사에서 예외 발생")
    public void 유효성_검사_테스트() throws Exception {
        //Given
        UserSignupDto dto = UserSignupDto.builder()
                .email("test12345678999@test.com")
                .name("tester")
                .password("123")
                .build();

        String jsonDto = objectMapper.writeValueAsString(dto);
        //When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDto))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect((ResultMatcher) jsonPath("$.code").value("C001"));
    }
}
