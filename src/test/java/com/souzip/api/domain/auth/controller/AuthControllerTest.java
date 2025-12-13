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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
    @DisplayName("카카오 로그인 - 신규 사용자")
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

        given(authService.login(eq(Provider.KAKAO), anyString()))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login/{provider}", "kakao")
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
                pathParameters(
                    parameterWithName("provider").description("OAuth Provider (kakao, google, apple)")
                ),
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
    @DisplayName("카카오 로그인 - 기존 사용자")
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

        given(authService.login(eq(Provider.KAKAO), anyString()))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login/{provider}", "kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.newUser").value(false))
            .andDo(document("auth/login-kakao-existing-user",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("provider").description("OAuth Provider")
                ),
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
                        .description("사용자 ID"),
                    fieldWithPath("data.user.nickname").type(JsonFieldType.STRING)
                        .description("사용자 닉네임"),
                    fieldWithPath("data.newUser").type(JsonFieldType.BOOLEAN)
                        .description("신규 사용자 여부 (false = 기존 사용자)"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("카카오 로그인 - 카카오 API 에러")
    void loginWithKakao_kakaoApiError() throws Exception {
        // given
        LoginRequest request = new LoginRequest("invalid_kakao_token");

        given(authService.login(eq(Provider.KAKAO), anyString()))
            .willThrow(new BusinessException(ErrorCode.KAKAO_API_ERROR));

        // when & then
        mockMvc.perform(post("/api/auth/login/{provider}", "kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("카카오 API 호출에 실패했습니다."))
            .andDo(document("auth/login-kakao-api-error",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("provider").description("OAuth Provider")
                ),
                requestFields(
                    fieldWithPath("accessToken").type(JsonFieldType.STRING)
                        .description("유효하지 않은 카카오 Access Token")
                ),
                responseFields(CommonDocumentation.errorResponseFields())
            ));
    }

    @Test
    @DisplayName("Google 로그인 - 신규 사용자")
    void loginWithGoogle_newUser() throws Exception {
        // given
        LoginRequest request = new LoginRequest("google_access_token");

        LoginUserInfo userInfo = new LoginUserInfo("abcd1234", "구글유저");
        LoginResponse response = LoginResponse.of(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access_token",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token",
            userInfo,
            true
        );

        given(authService.login(eq(Provider.GOOGLE), anyString()))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login/{provider}", "google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.newUser").value(true))
            .andDo(document("auth/login-google-new-user",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("provider").description("OAuth Provider (google)")
                ),
                requestFields(
                    fieldWithPath("accessToken").type(JsonFieldType.STRING)
                        .description("구글 Access Token")
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
                        .description("사용자 ID"),
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
    @DisplayName("Google 로그인 - 기존 사용자")
    void loginWithGoogle_existingUser() throws Exception {
        // given
        LoginRequest request = new LoginRequest("google_access_token");

        LoginUserInfo userInfo = new LoginUserInfo("abcd1234", "구글유저");
        LoginResponse response = LoginResponse.of(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access_token",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token",
            userInfo,
            false
        );

        given(authService.login(eq(Provider.GOOGLE), anyString()))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login/{provider}", "google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.newUser").value(false));
    }

    @Test
    @DisplayName("Apple 로그인 - 신규 사용자")
    void loginWithApple_newUser() throws Exception {
        // given
        LoginRequest request = new LoginRequest("apple_id_token");

        LoginUserInfo userInfo = new LoginUserInfo("xyz98765", "애플유저");
        LoginResponse response = LoginResponse.of(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access_token",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token",
            userInfo,
            true
        );

        given(authService.login(eq(Provider.APPLE), anyString()))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login/{provider}", "apple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.newUser").value(true))
            .andDo(document("auth/login-apple-new-user",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("provider").description("OAuth Provider (apple)")
                ),
                requestFields(
                    fieldWithPath("accessToken").type(JsonFieldType.STRING)
                        .description("애플 ID Token")
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
                        .description("사용자 ID"),
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
    @DisplayName("Apple 로그인 - 기존 사용자")
    void loginWithApple_existingUser() throws Exception {
        // given
        LoginRequest request = new LoginRequest("apple_id_token");

        LoginUserInfo userInfo = new LoginUserInfo("xyz98765", "애플유저");
        LoginResponse response = LoginResponse.of(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access_token",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh_token",
            userInfo,
            false
        );

        given(authService.login(eq(Provider.APPLE), anyString()))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login/{provider}", "apple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.newUser").value(false));
    }

    @Test
    @DisplayName("지원하지 않는 Provider - 400 에러")
    void loginWithUnsupportedProvider_returns400() throws Exception {
        // given
        LoginRequest request = new LoginRequest("naver_access_token");

        // when & then
        mockMvc.perform(post("/api/auth/login/{provider}", "naver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("지원하지 않는 로그인 방식입니다: naver"))
            .andDo(document("auth/login-unsupported-provider",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("provider").description("지원하지 않는 Provider (naver)")
                ),
                requestFields(
                    fieldWithPath("accessToken").type(JsonFieldType.STRING)
                        .description("OAuth Access Token")
                ),
                responseFields(CommonDocumentation.errorResponseFields())
            ));
    }

    @Test
    @DisplayName("잘못된 Provider 형식 - 400 에러")
    void loginWithInvalidProvider_returns400() throws Exception {
        // given
        LoginRequest request = new LoginRequest("invalid_token");

        // when & then
        mockMvc.perform(post("/api/auth/login/{provider}", "invalid123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andDo(document("auth/login-invalid-provider",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("provider").description("잘못된 형식의 Provider")
                ),
                requestFields(
                    fieldWithPath("accessToken").type(JsonFieldType.STRING)
                        .description("OAuth Access Token")
                ),
                responseFields(CommonDocumentation.errorResponseFields())
            ));
    }

    @Test
    @DisplayName("대소문자 구분 없이 Provider 처리")
    void loginWithCaseInsensitiveProvider_success() throws Exception {
        // given
        LoginRequest request = new LoginRequest("kakao_token");

        LoginUserInfo userInfo = new LoginUserInfo("20251204", "수집");
        LoginResponse response = LoginResponse.of(
            "access_token",
            "refresh_token",
            userInfo,
            true
        );

        given(authService.login(eq(Provider.KAKAO), anyString()))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login/{provider}", "KAKAO")  // 대문자
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    @DisplayName("Refresh Token 재발급 - Access Token만")
    void refresh_withValidToken_returnsNewAccessTokenOnly() throws Exception {
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
    @DisplayName("Refresh Token 만료 임박 - 둘 다 재발급")
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
            .andExpect(jsonPath("$.data.refreshToken").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_refresh_token"))
            .andDo(document("auth/refresh-expiring-soon",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                        .description("Refresh Token (만료 10일 이하)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT)
                        .description("토큰 재발급 응답 데이터"),
                    fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                        .description("새로 발급된 JWT Access Token"),
                    fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
                        .description("새로 발급된 JWT Refresh Token"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("만료된 Refresh Token - 401 에러")
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
    @DisplayName("유효하지 않은 Refresh Token - 401 에러")
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
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        // given
        doNothing().when(authService).logout(anyLong());

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer valid_access_token"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."))
            .andDo(document("auth/logout",
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
