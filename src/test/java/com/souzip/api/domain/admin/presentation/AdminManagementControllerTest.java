package com.souzip.api.domain.admin.presentation;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.admin.application.AdminManagementService;
import com.souzip.api.domain.admin.exception.AdminErrorCode;
import com.souzip.api.domain.admin.exception.AdminException;
import com.souzip.api.domain.admin.fixture.TestAdminPasswordEncoder;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.presentation.request.InviteAdminRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static com.souzip.api.docs.CommonDocumentation.errorResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminManagementControllerTest extends RestDocsSupport {

    private final AdminManagementService adminManagementService = mock(AdminManagementService.class);

    @Override
    protected Object initController() {
        return new AdminManagementController(adminManagementService);
    }

    @Test
    @DisplayName("관리자 초대 - ADMIN 역할")
    void inviteAdmin_withAdminRole_success() throws Exception {
        // given
        setSuperAdminAuthentication();

        InviteAdminRequest request = new InviteAdminRequest(
            "newadmin",
            "password123",
            AdminRole.ADMIN
        );

        Admin createdAdmin = Admin.create("newadmin", "password123", AdminRole.ADMIN,
            new TestAdminPasswordEncoder());

        given(adminManagementService.inviteAdmin(any())).willReturn(createdAdmin);

        // when & then
        mockMvc.perform(post("/api/admin/invite")
                .header("Authorization", "Bearer super-admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("newadmin"))
            .andExpect(jsonPath("$.data.role").value("ADMIN"))
            .andExpect(jsonPath("$.message").value("관리자 초대가 완료되었습니다."))
            .andDo(document("admin/invite-admin",
                getDocumentRequest(),
                getDocumentResponse(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer {accessToken} - SUPER_ADMIN 권한 필요")
                ),
                requestFields(
                    fieldWithPath("username").type(JsonFieldType.STRING)
                        .description("아이디 (4-20자, 영문/숫자/언더스코어)"),
                    fieldWithPath("password").type(JsonFieldType.STRING)
                        .description("비밀번호 (최소 8자)"),
                    fieldWithPath("role").type(JsonFieldType.STRING)
                        .description("역할 (ADMIN 또는 VIEWER)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("생성된 관리자 정보"),
                    fieldWithPath("data.adminId").type(JsonFieldType.STRING).description("관리자 ID (UUID)"),
                    fieldWithPath("data.username").type(JsonFieldType.STRING).description("아이디"),
                    fieldWithPath("data.role").type(JsonFieldType.STRING).description("역할"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                )
            ));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("관리자 초대 - VIEWER 역할")
    void inviteAdmin_withViewerRole_success() throws Exception {
        // given
        setSuperAdminAuthentication();

        InviteAdminRequest request = new InviteAdminRequest(
            "viewer01",
            "password123",
            AdminRole.VIEWER
        );

        Admin createdAdmin = Admin.create("viewer01", "password123", AdminRole.VIEWER,
            new TestAdminPasswordEncoder());

        given(adminManagementService.inviteAdmin(any())).willReturn(createdAdmin);

        // when & then
        mockMvc.perform(post("/api/admin/invite")
                .header("Authorization", "Bearer super-admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("viewer01"))
            .andExpect(jsonPath("$.data.role").value("VIEWER"))
            .andDo(document("admin/invite-viewer",
                getDocumentRequest(),
                getDocumentResponse(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer {accessToken}")
                ),
                requestFields(
                    fieldWithPath("username").type(JsonFieldType.STRING).description("아이디"),
                    fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                    fieldWithPath("role").type(JsonFieldType.STRING).description("역할 (VIEWER)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("생성된 관리자 정보"),
                    fieldWithPath("data.adminId").type(JsonFieldType.STRING).description("관리자 ID"),
                    fieldWithPath("data.username").type(JsonFieldType.STRING).description("아이디"),
                    fieldWithPath("data.role").type(JsonFieldType.STRING).description("역할"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                )
            ));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("SUPER_ADMIN 역할 초대 시도 - 400 에러")
    void inviteAdmin_withSuperAdminRole_fails() throws Exception {
        // given
        setSuperAdminAuthentication();

        InviteAdminRequest request = new InviteAdminRequest(
            "superadmin",
            "password123",
            AdminRole.SUPER_ADMIN
        );

        given(adminManagementService.inviteAdmin(any()))
            .willThrow(new AdminException(AdminErrorCode.CANNOT_INVITE_SUPER_ADMIN));

        // when & then
        mockMvc.perform(post("/api/admin/invite")
                .header("Authorization", "Bearer super-admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("최고 관리자는 초대할 수 없습니다."))
            .andDo(document("admin/invite-super-admin-fails",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("username").type(JsonFieldType.STRING).description("아이디"),
                    fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                    fieldWithPath("role").type(JsonFieldType.STRING).description("역할 (SUPER_ADMIN - 불가)")
                ),
                responseFields(errorResponseFields())
            ));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("중복된 아이디로 초대 시도 - 409 에러")
    void inviteAdmin_withDuplicateUsername_fails() throws Exception {
        // given
        setSuperAdminAuthentication();

        InviteAdminRequest request = new InviteAdminRequest(
            "existing",
            "password123",
            AdminRole.ADMIN
        );

        given(adminManagementService.inviteAdmin(any()))
            .willThrow(new AdminException(AdminErrorCode.ADMIN_USERNAME_DUPLICATED));

        // when & then
        mockMvc.perform(post("/api/admin/invite")
                .header("Authorization", "Bearer super-admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."))
            .andDo(document("admin/invite-duplicate-username",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("username").type(JsonFieldType.STRING).description("중복된 아이디"),
                    fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                    fieldWithPath("role").type(JsonFieldType.STRING).description("역할")
                ),
                responseFields(errorResponseFields())
            ));

        SecurityContextHolder.clearContext();
    }

    private void setSuperAdminAuthentication() {
        Admin superAdmin = Admin.create("superadmin", "password", AdminRole.SUPER_ADMIN,
            new TestAdminPasswordEncoder());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                superAdmin,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
