package com.souzip.api.domain.admin.presentation;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.admin.application.AdminAuthService;
import com.souzip.api.domain.admin.application.AdminAuthService.AdminLoginResult;
import com.souzip.api.domain.admin.fixture.TestAdminPasswordEncoder;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.presentation.request.AdminLoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAuthControllerTest extends RestDocsSupport {

    private final AdminAuthService adminAuthService = mock(AdminAuthService.class);

    @Override
    protected Object initController() {
        return new AdminAuthController(adminAuthService);
    }

    @Test
    @DisplayName("어드민 로그인에 성공한다.")
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
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
            .andExpect(jsonPath("$.data.username").value("admin123"))
            .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"))
            .andDo(document("admin-login",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("username").type(JsonFieldType.STRING).description("어드민 아이디"),
                    fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                ),
                responseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                    fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
                    fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                    fieldWithPath("data.id").type(JsonFieldType.STRING).description("어드민 ID"),
                    fieldWithPath("data.username").type(JsonFieldType.STRING).description("어드민 아이디"),
                    fieldWithPath("data.role").type(JsonFieldType.STRING).description("권한"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }
}
