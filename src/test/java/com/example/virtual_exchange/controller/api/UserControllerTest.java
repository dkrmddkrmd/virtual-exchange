package com.example.virtual_exchange.controller.api;

import com.example.virtual_exchange.config.auth.JwtUtil;
import com.example.virtual_exchange.config.auth.PrincipalDetailsService;
import com.example.virtual_exchange.domain.User;
import com.example.virtual_exchange.dto.UserSignupDto;
import com.example.virtual_exchange.service.RefreshTokenService;
import com.example.virtual_exchange.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @MockitoBean
    private RefreshTokenService refreshTokenService;

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
        mockMvc.perform(post("/api/users/signup")
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
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDto))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect((ResultMatcher) jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("Refresh Token으로 재발급 성공")
    public void 재발급_테스트() throws Exception {
        // Given
        Mockito.when(refreshTokenService.rotate(Mockito.any()))
                .thenReturn(new RefreshTokenService.ReissueResult("newAccessToken", "newRefreshToken"));

        // When & Then
        mockMvc.perform(post("/api/users/reissue")
                        .cookie(new Cookie("refreshToken", "validToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(cookie().exists("refreshToken"))          // 쿠키 존재 확인
                .andExpect(cookie().httpOnly("refreshToken", true))  // HttpOnly 확인
                .andDo(document("user-reissue",
                        responseFields(
                                fieldWithPath("grantType").description("토큰 타입"),
                                fieldWithPath("accessToken").description("새로 발급된 Access Token")
                        )));
    }

    @Test
    @DisplayName("로그아웃 시 refresh token 쿠키가 삭제된다.")
    public void 로그아웃_테스트() throws Exception {
        // Given
        Mockito.when(refreshTokenService.extractUserId(Mockito.any()))
                .thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/users/logout")
                        .cookie(new Cookie("refreshToken", "validToken")))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refreshToken", 0)) // 쿠키 만료 확인
                .andDo(document("user-logout"));
    }
}
