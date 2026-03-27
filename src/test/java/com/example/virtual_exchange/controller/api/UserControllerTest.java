package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.config.auth.JwtUtil;
import com.example.virtual_exchange.config.auth.PrincipalDetailsService;
import com.example.virtual_exchange.dto.UserSignupDto;
import com.example.virtual_exchange.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private PrincipalDetailsService principalDetailsService;

    @Test
    @DisplayName("정상적인 데이터로 회원가입 시 성공하고 문서가 추출된다")
    public void 회원가입_성공_문서화_테스트() throws Exception{
        //Given
        UserSignupDto dto = UserSignupDto.builder()
                .email("apiTest@test.com")
                .name("apiTester")
                .password("1234")
                .build();

        String jsonDto = objectMapper.writeValueAsString(dto);

        //When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDto))
                .andExpect(status().isCreated())
                //Rest Docs 문서 추출
                .andDo(document("user-signup", requestFields(
                        fieldWithPath("email").description("사용자 이메일"),
                        fieldWithPath("name").description("사용자 이름"),
                        fieldWithPath("password").description("비밀번호 (4자리 이상)")
                )));

    }

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
