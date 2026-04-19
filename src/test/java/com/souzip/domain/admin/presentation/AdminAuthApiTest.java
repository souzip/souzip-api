package com.souzip.domain.admin.presentation;

import com.souzip.docs.CommonDocumentation;
import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.admin.application.AdminAuthService;
import com.souzip.domain.admin.application.AdminAuthService.AdminLoginResult;
import com.souzip.domain.admin.application.AdminAuthService.RefreshResult;
import com.souzip.domain.admin.exception.AdminExpiredRefreshTokenException;
import com.souzip.domain.admin.exception.AdminInvalidRefreshTokenException;
import com.souzip.domain.admin.fixture.TestAdminPasswordEncoder;
import com.souzip.domain.admin.model.Admin;
import com.souzip.domain.admin.model.AdminRole;
import com.souzip.domain.admin.presentation.request.AdminLoginRequest;
import com.souzip.domain.admin.presentation.request.AdminRefreshRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.UUID;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAuthApiTest extends RestDocsSupport {

    private final AdminAuthService adminAuthService = mock(AdminAuthService.class);

    @Override
    protected Object initController() {
        return new AdminAuthController(adminAuthService);
    }

    @Test
    @DisplayName("어드민 로그인 성공")
    void login_success() throws Exception {
        // given
        AdminLoginRequest request = new AdminLoginRequest("admin123", "password123");

        Admin mockAdmin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN,
                new TestAdminPasswordEncoder());

        AdminLoginResult result = new AdminLoginResult(mockAdmin, "access-token", "refresh-token");

        given(adminAuthService.login(any())).willReturn(result);

        // when & then
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.username").value("admin123"))
                .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"))
                .andDo(document("admin/login",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("username").type(JsonFieldType.STRING).description("어드민 아이디 (4~10자)"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("로그인 응답 데이터"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("JWT Access Token"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("JWT Refresh Token"),
                                fieldWithPath("data.id").type(JsonFieldType.STRING).description("어드민 ID (UUID)"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("어드민 아이디"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING).description("권한 (SUPER_ADMIN)"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("어드민 토큰 갱신 - Access Token만")
    void refresh_withValidToken_returnsNewAccessTokenOnly() throws Exception {
        // given
        AdminRefreshRequest request = new AdminRefreshRequest("valid-refresh-token");
        RefreshResult result = new RefreshResult("new-access-token", "valid-refresh-token");

        given(adminAuthService.refresh(anyString())).willReturn(result);

        // when & then
        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("valid-refresh-token"))
                .andDo(document("admin/refresh-valid-token",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("Refresh Token")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("토큰 재발급 응답 데이터"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("새로 발급된 JWT Access Token"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
                                        .description("Refresh Token (유효기간 10일 초과 시 그대로 유지)"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("어드민 토큰 갱신 - 만료 임박 시 둘 다 재발급")
    void refresh_withExpiringSoon_returnsBothNewTokens() throws Exception {
        // given
        AdminRefreshRequest request = new AdminRefreshRequest("expiring-soon-token");
        RefreshResult result = new RefreshResult("new-access-token", "new-refresh-token");

        given(adminAuthService.refresh(anyString())).willReturn(result);

        // when & then
        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"))
                .andDo(document("admin/refresh-expiring-soon",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                                        .description("Refresh Token (만료 10일 이하)")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("토큰 재발급 응답 데이터"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("새로 발급된 JWT Access Token"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("새로 발급된 JWT Refresh Token"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("만료된 Refresh Token - 401 에러")
    void refresh_withExpiredToken_returns401() throws Exception {
        // given
        AdminRefreshRequest request = new AdminRefreshRequest("expired-refresh-token");
        given(adminAuthService.refresh(anyString()))
                .willThrow(new AdminExpiredRefreshTokenException());

        // when & then
        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("만료된 리프레시 토큰입니다."))
                .andDo(document("admin/refresh-expired-token",
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
        AdminRefreshRequest request = new AdminRefreshRequest("invalid-refresh-token");
        given(adminAuthService.refresh(anyString()))
                .willThrow(new AdminInvalidRefreshTokenException());

        // when & then
        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."))
                .andDo(document("admin/refresh-invalid-token",
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
    @DisplayName("어드민 로그아웃 성공")
    void logout_success() throws Exception {
        // given
        Admin mockAdmin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN,
                new TestAdminPasswordEncoder());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockAdmin, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        doNothing().when(adminAuthService).logout(any(UUID.class));

        // when & then
        mockMvc.perform(post("/api/admin/auth/logout")
                        .header("Authorization", "Bearer valid-access-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다."))
                .andDo(document("admin/logout",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer {accessToken}")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)").optional(),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));

        SecurityContextHolder.clearContext();
    }
}
