package com.souzip.adapter.webapi.admin;

import com.souzip.adapter.webapi.admin.dto.AdminLoginRequest;
import com.souzip.adapter.webapi.admin.dto.AdminRefreshRequest;
import com.souzip.application.admin.AdminAuthService;
import com.souzip.application.admin.dto.AdminLoginResult;
import com.souzip.application.admin.dto.AdminRefreshResult;
import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.AdminFixture;
import com.souzip.domain.admin.exception.AdminExpiredRefreshTokenException;
import com.souzip.domain.admin.exception.AdminInvalidRefreshTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.UUID;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAuthApiTest extends RestDocsSupport {

    private final AdminAuthService adminAuthService = mock(AdminAuthService.class);

    @Override
    protected Object initController() {
        return new AdminAuthApi(adminAuthService);
    }

    @DisplayName("어드민 로그인")
    @Test
    void login() throws Exception {
        Admin admin = AdminFixture.createAdmin();
        AdminLoginResult result = new AdminLoginResult(admin, "access-token", "refresh-token");
        given(adminAuthService.login(any(), any())).willReturn(result);

        AdminLoginRequest request = new AdminLoginRequest("admin123", "password123");

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andDo(document("admin/login",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("username").type(JsonFieldType.STRING).description("어드민 아이디"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.STRING).description("어드민 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("아이디"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING).description("역할"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰 재발급")
    @Test
    void refresh_validToken() throws Exception {
        AdminRefreshResult result = new AdminRefreshResult("new-access-token", "refresh-token");
        given(adminAuthService.refresh(any())).willReturn(result);

        AdminRefreshRequest request = new AdminRefreshRequest("refresh-token");

        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("admin/refresh-valid-token",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("새 액세스 토큰"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("만료 임박 리프레시 토큰으로 재발급 시 토큰도 갱신")
    @Test
    void refresh_expiringSoon() throws Exception {
        AdminRefreshResult result = new AdminRefreshResult("new-access-token", "new-refresh-token");
        given(adminAuthService.refresh(any())).willReturn(result);

        AdminRefreshRequest request = new AdminRefreshRequest("expiring-soon-token");

        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("admin/refresh-expiring-soon",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("만료 임박 리프레시 토큰")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("새 액세스 토큰"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("새 리프레시 토큰"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("만료된 리프레시 토큰으로 재발급 시 예외 발생")
    @Test
    void refresh_expiredToken() throws Exception {
        given(adminAuthService.refresh(any()))
                .willThrow(new AdminExpiredRefreshTokenException());

        AdminRefreshRequest request = new AdminRefreshRequest("expired-token");

        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andDo(document("admin/refresh-expired-token",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("만료된 리프레시 토큰")
                        ),
                        responseFields(
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("트레이스 ID"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @DisplayName("유효하지 않은 리프레시 토큰으로 재발급 시 예외 발생")
    @Test
    void refresh_invalidToken() throws Exception {
        given(adminAuthService.refresh(any()))
                .willThrow(new AdminInvalidRefreshTokenException());

        AdminRefreshRequest request = new AdminRefreshRequest("invalid-token");

        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andDo(document("admin/refresh-invalid-token",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("유효하지 않은 리프레시 토큰")
                        ),
                        responseFields(
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("트레이스 ID"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @DisplayName("어드민 로그아웃")
    @Test
    void logout() throws Exception {
        willDoNothing().given(adminAuthService).logout(any(UUID.class));

        mockMvc.perform(post("/api/admin/auth/logout")
                        .header("Authorization", "Bearer access-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("admin/logout",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        apiResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }
}