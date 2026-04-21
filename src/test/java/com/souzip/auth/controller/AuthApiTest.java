package com.souzip.auth.controller;

import com.souzip.auth.adapter.web.AuthApi;
import com.souzip.auth.adapter.web.dto.LoginRequest;
import com.souzip.auth.adapter.web.dto.RefreshRequest;
import com.souzip.auth.application.dto.LoginInfo;
import com.souzip.auth.application.dto.RefreshInfo;
import com.souzip.auth.application.exception.AuthException;
import com.souzip.auth.application.provided.Auth;
import com.souzip.docs.CommonDocumentation;
import com.souzip.docs.RestDocsSupport;
import com.souzip.shared.domain.Provider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiTest extends RestDocsSupport {

    private static final String PROVIDER_DESC = "OAuth Provider (kakao, google, apple)";

    private final Auth auth = mock(Auth.class);

    @Override
    protected Object initController() {
        return new AuthApi(auth);
    }

    private LoginInfo createLoginInfo(boolean needsOnboarding) {
        return new LoginInfo(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access_token",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token",
                needsOnboarding,
                "20251204",
                "수집"
        );
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[] loginResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[]{
                fieldWithPath("data").type(JsonFieldType.OBJECT).description("로그인 응답 데이터"),
                fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("JWT Access Token"),
                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("JWT Refresh Token"),
                fieldWithPath("data.user").type(JsonFieldType.OBJECT).description("사용자 정보"),
                fieldWithPath("data.user.userId").type(JsonFieldType.STRING).description("사용자 ID"),
                fieldWithPath("data.user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                fieldWithPath("data.needsOnboarding").type(JsonFieldType.BOOLEAN).description("온보딩 필요 여부"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
        };
    }

    @Test
    @DisplayName("카카오 로그인 - 신규 사용자")
    void loginWithKakao_newUser() throws Exception {
        LoginRequest request = new LoginRequest("kakao_access_token");
        given(auth.login(eq(Provider.KAKAO), anyString())).willReturn(createLoginInfo(true));

        mockMvc.perform(post("/api/auth/login/{provider}", "kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.userId").value("20251204"))
                .andExpect(jsonPath("$.data.user.nickname").value("수집"))
                .andExpect(jsonPath("$.data.needsOnboarding").value(true))
                .andDo(document("auth/login-kakao-new-user",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("provider").description(PROVIDER_DESC)),
                        requestFields(fieldWithPath("accessToken").type(JsonFieldType.STRING).description("카카오 Access Token")),
                        apiResponseFields(loginResponseFields())
                ));
    }

    @Test
    @DisplayName("카카오 로그인 - 기존 사용자")
    void loginWithKakao_existingUser() throws Exception {
        LoginRequest request = new LoginRequest("kakao_access_token");
        given(auth.login(eq(Provider.KAKAO), anyString())).willReturn(createLoginInfo(false));

        mockMvc.perform(post("/api/auth/login/{provider}", "kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.needsOnboarding").value(false))
                .andDo(document("auth/login-kakao-existing-user",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("provider").description(PROVIDER_DESC)),
                        requestFields(fieldWithPath("accessToken").type(JsonFieldType.STRING).description("카카오 Access Token")),
                        apiResponseFields(loginResponseFields())
                ));
    }

    @Test
    @DisplayName("카카오 로그인 - 카카오 API 에러")
    void loginWithKakao_kakaoApiError() throws Exception {
        LoginRequest request = new LoginRequest("invalid_kakao_token");
        given(auth.login(eq(Provider.KAKAO), anyString())).willThrow(AuthException.kakaoApiError());

        mockMvc.perform(post("/api/auth/login/{provider}", "kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("카카오 API 호출에 실패했습니다."))
                .andDo(document("auth/login-kakao-api-error",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("provider").description(PROVIDER_DESC)),
                        requestFields(fieldWithPath("accessToken").type(JsonFieldType.STRING).description("유효하지 않은 카카오 Access Token")),
                        responseFields(CommonDocumentation.errorResponseFields())
                ));
    }

    @Test
    @DisplayName("Google 로그인 - 신규 사용자")
    void loginWithGoogle_newUser() throws Exception {
        LoginRequest request = new LoginRequest("google_access_token");
        given(auth.login(eq(Provider.GOOGLE), anyString())).willReturn(createLoginInfo(true));

        mockMvc.perform(post("/api/auth/login/{provider}", "google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.needsOnboarding").value(true))
                .andDo(document("auth/login-google-new-user",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("provider").description(PROVIDER_DESC)),
                        requestFields(fieldWithPath("accessToken").type(JsonFieldType.STRING).description("구글 Access Token")),
                        apiResponseFields(loginResponseFields())
                ));
    }

    @Test
    @DisplayName("Google 로그인 - 기존 사용자")
    void loginWithGoogle_existingUser() throws Exception {
        LoginRequest request = new LoginRequest("google_access_token");
        given(auth.login(eq(Provider.GOOGLE), anyString())).willReturn(createLoginInfo(false));

        mockMvc.perform(post("/api/auth/login/{provider}", "google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.needsOnboarding").value(false))
                .andDo(document("auth/login-google-existing-user",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("provider").description(PROVIDER_DESC)),
                        requestFields(fieldWithPath("accessToken").type(JsonFieldType.STRING).description("구글 Access Token")),
                        apiResponseFields(loginResponseFields())
                ));
    }

    @Test
    @DisplayName("Apple 로그인 - 신규 사용자")
    void loginWithApple_newUser() throws Exception {
        LoginRequest request = new LoginRequest("apple_id_token");
        given(auth.login(eq(Provider.APPLE), anyString())).willReturn(createLoginInfo(true));

        mockMvc.perform(post("/api/auth/login/{provider}", "apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.needsOnboarding").value(true))
                .andDo(document("auth/login-apple-new-user",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("provider").description(PROVIDER_DESC)),
                        requestFields(fieldWithPath("accessToken").type(JsonFieldType.STRING).description("애플 ID Token")),
                        apiResponseFields(loginResponseFields())
                ));
    }

    @Test
    @DisplayName("Apple 로그인 - 기존 사용자")
    void loginWithApple_existingUser() throws Exception {
        LoginRequest request = new LoginRequest("apple_id_token");
        given(auth.login(eq(Provider.APPLE), anyString())).willReturn(createLoginInfo(false));

        mockMvc.perform(post("/api/auth/login/{provider}", "apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.needsOnboarding").value(false))
                .andDo(document("auth/login-apple-existing-user",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("provider").description(PROVIDER_DESC)),
                        requestFields(fieldWithPath("accessToken").type(JsonFieldType.STRING).description("애플 ID Token")),
                        apiResponseFields(loginResponseFields())
                ));
    }

    @Test
    @DisplayName("지원하지 않는 Provider - 400 에러")
    void loginWithUnsupportedProvider_returns400() throws Exception {
        LoginRequest request = new LoginRequest("naver_access_token");
        given(auth.login(any(), anyString())).willThrow(AuthException.unsupportedProvider());

        mockMvc.perform(post("/api/auth/login/{provider}", "naver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(document("auth/login-unsupported-provider",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("provider").description(PROVIDER_DESC)),
                        requestFields(fieldWithPath("accessToken").type(JsonFieldType.STRING).description("OAuth Access Token")),
                        responseFields(CommonDocumentation.errorResponseFields())
                ));
    }

    @Test
    @DisplayName("잘못된 Provider 형식 - 400 에러")
    void loginWithInvalidProvider_returns400() throws Exception {
        LoginRequest request = new LoginRequest("invalid_token");
        given(auth.login(any(), anyString())).willThrow(AuthException.unsupportedProvider());

        mockMvc.perform(post("/api/auth/login/{provider}", "invalid123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(document("auth/login-invalid-provider",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(parameterWithName("provider").description(PROVIDER_DESC)),
                        requestFields(fieldWithPath("accessToken").type(JsonFieldType.STRING).description("OAuth Access Token")),
                        responseFields(CommonDocumentation.errorResponseFields())
                ));
    }

    @Test
    @DisplayName("Refresh Token 재발급")
    void refresh_withValidToken() throws Exception {
        RefreshRequest request = new RefreshRequest("valid_refresh_token");
        given(auth.refresh(anyString())).willReturn(
                new RefreshInfo("new_access_token", "valid_refresh_token")
        );

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new_access_token"))
                .andExpect(jsonPath("$.data.refreshToken").value("valid_refresh_token"))
                .andDo(document("auth/refresh-valid-token",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("Refresh Token")),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("토큰 재발급 응답"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("새 Access Token"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("Refresh Token"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("Refresh Token 만료 임박 - 둘 다 재발급")
    void refresh_withExpiringSoon_returnsBothNewTokens() throws Exception {
        RefreshRequest request = new RefreshRequest("expiring_soon_token");
        given(auth.refresh(anyString())).willReturn(
                new RefreshInfo("new_access_token", "new_refresh_token")
        );

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.refreshToken").value("new_refresh_token"))
                .andDo(document("auth/refresh-expiring-soon",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("Refresh Token (만료 10일 이하)")),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("토큰 재발급 응답"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("새 Access Token"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("새 Refresh Token"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("만료된 Refresh Token - 401 에러")
    void refresh_withExpiredToken_returns401() throws Exception {
        RefreshRequest request = new RefreshRequest("expired_refresh_token");
        given(auth.refresh(anyString())).willThrow(AuthException.expiredRefreshToken());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("만료된 Refresh Token입니다."))
                .andDo(document("auth/refresh-expired-token",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("만료된 Refresh Token")),
                        responseFields(CommonDocumentation.errorResponseFields())
                ));
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token - 401 에러")
    void refresh_withInvalidToken_returns401() throws Exception {
        RefreshRequest request = new RefreshRequest("invalid_refresh_token");
        given(auth.refresh(anyString())).willThrow(AuthException.invalidRefreshToken());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 Refresh Token입니다."))
                .andDo(document("auth/refresh-invalid-token",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("유효하지 않은 Refresh Token")),
                        responseFields(CommonDocumentation.errorResponseFields())
                ));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        doNothing().when(auth).logout(anyLong());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer valid_access_token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."))
                .andDo(document("auth/logout",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }
}