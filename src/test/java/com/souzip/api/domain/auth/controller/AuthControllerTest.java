package com.souzip.api.domain.auth.controller;

import com.souzip.api.docs.CommonDocumentation;
import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.auth.dto.LoginRequest;
import com.souzip.api.domain.auth.dto.LoginResponse;
import com.souzip.api.domain.auth.dto.LoginUserInfo;
import com.souzip.api.domain.auth.dto.RefreshRequest;
import com.souzip.api.domain.auth.dto.RefreshResponse;
import com.souzip.api.domain.auth.service.AuthService;
import com.souzip.api.domain.user.entity.Provider;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends RestDocsSupport {

    private final AuthService authService = mock(AuthService.class);

    @Override
    protected Object initController() {
        return new AuthController(authService);
    }

    @Test
    @DisplayName("카카오 로그인을 한다. (신규 사용자)")
    void loginWithKakao_newUser() throws Exception {
        // given
        LoginRequest request = new LoginRequest("kakao_access_token");

        LoginUserInfo userInfo = new LoginUserInfo("20251204", "수집");
        LoginResponse response = LoginResponse.of(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access_token",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token",
            userInfo,
            true
        );

        given(authService.login(any(Provider.class), anyString()))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.user.userId").value("20251204"))
            .andExpect(jsonPath("$.data.user.nickname").value("수집"))
            .andExpect(jsonPath("$.data.newUser").value(true))
            .andDo(document("auth/login-kakao-new-user",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("accessToken").type(JsonFieldType.STRING)
                        .description("카카오 Access Token")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("로그인 응답 데이터"),
                    fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                        .description("JWT Access Token"),
                    fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
                        .description("JWT Refresh Token"),
                    fieldWithPath("data.user").type(JsonFieldType.OBJECT)
                        .description("사용자 정보"),
                    fieldWithPath("data.user.userId").type(JsonFieldType.STRING)
                        .description("사용자 ID (UUID 앞 8자리)"),
                    fieldWithPath("data.user.nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임"),
                    fieldWithPath("data.newUser").type(JsonFieldType.BOOLEAN)
                        .description("신규 사용자 여부"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("카카오 로그인을 한다. (기존 사용자)")
    void loginWithKakao_existingUser() throws Exception {
        // given
        LoginRequest request = new LoginRequest("kakao_access_token");

        LoginUserInfo userInfo = new LoginUserInfo("20251204", "수집");
        LoginResponse response = LoginResponse.of(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access_token",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token",
            userInfo,
            false
        );

        given(authService.login(any(Provider.class), anyString()))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.user.userId").value("20251204"))
            .andExpect(jsonPath("$.data.user.nickname").value("수집"))
            .andExpect(jsonPath("$.data.newUser").value(false))
            .andDo(document("auth/login-kakao-existing-user",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("accessToken").type(JsonFieldType.STRING)
                        .description("카카오 Access Token")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("로그인 응답 데이터"),
                    fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                        .description("JWT Access Token"),
                    fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
                        .description("JWT Refresh Token"),
                    fieldWithPath("data.user").type(JsonFieldType.OBJECT)
                        .description("사용자 정보"),
                    fieldWithPath("data.user.userId").type(JsonFieldType.STRING)
                        .description("사용자 ID (UUID 앞 8자리)"),
                    fieldWithPath("data.user.nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임"),
                    fieldWithPath("data.newUser").type(JsonFieldType.BOOLEAN)
                        .description("신규 사용자 여부"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("Refresh Token으로 토큰을 재발급한다. (Access Token만)")
    void refresh_withValidToken_success() throws Exception {
        // given
        RefreshRequest request = new RefreshRequest("valid_refresh_token");
        RefreshResponse response = RefreshResponse.of(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_access_token",
            "valid_refresh_token"
        );

        given(authService.refresh(anyString())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_access_token"))
            .andExpect(jsonPath("$.data.refreshToken").value("valid_refresh_token"))
            .andDo(document("auth/refresh-valid-token",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                        .description("Refresh Token")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("토큰 재발급 응답 데이터"),
                    fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                        .description("새로 발급된 JWT Access Token"),
                    fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
                        .description("Refresh Token (유효기간 10일 초과 시 그대로 유지)"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("Refresh Token 만료 임박 시 둘 다 재발급한다.")
    void refresh_withExpiringSoon_returnsBothNewTokens() throws Exception {
        // given
        RefreshRequest request = new RefreshRequest("expiring_soon_token");
        RefreshResponse response = RefreshResponse.of(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_access_token",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_refresh_token"
        );

        given(authService.refresh(anyString())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_access_token"))
            .andExpect(jsonPath("$.data.refreshToken").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_refresh_token"))
            .andDo(document("auth/refresh-expiring-soon",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                        .description("Refresh Token (만료 10일 이하 남음)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("토큰 재발급 응답 데이터"),
                    fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                        .description("새로 발급된 JWT Access Token"),
                    fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
                        .description("새로 발급된 JWT Refresh Token (만료 10일 이하 시 갱신)"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("만료된 Refresh Token으로 재발급 시 401 에러를 반환한다.")
    void refresh_withExpiredToken_returns401() throws Exception {
        // given
        RefreshRequest request = new RefreshRequest("expired_refresh_token");
        given(authService.refresh(anyString()))
            .willThrow(new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN));

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("만료된 Refresh Token입니다."))
            .andDo(document("auth/refresh-expired-token",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                        .description("만료된 Refresh Token")
                ),
                responseFields(CommonDocumentation.errorResponseFields())
            ));
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 재발급 시 401 에러를 반환한다.")
    void refresh_withInvalidToken_returns401() throws Exception {
        // given
        RefreshRequest request = new RefreshRequest("invalid_refresh_token");
        given(authService.refresh(anyString()))
            .willThrow(new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("유효하지 않은 Refresh Token입니다."))
            .andDo(document("auth/refresh-invalid-token",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                        .description("유효하지 않은 Refresh Token")
                ),
                responseFields(CommonDocumentation.errorResponseFields())
            ));
    }

    @Test
    @DisplayName("로그아웃을 한다")
    void logout_success() throws Exception {
        // given
        doNothing().when(authService).logout(anyLong());

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer valid_access_token"))
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("auth/logout",
                getDocumentRequest(),
                getDocumentResponse(),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.NULL)
                        .description("응답 데이터 (null)"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지").optional()
                )
            ));
    }
}
