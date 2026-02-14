package com.souzip.api.domain.admin.presentation;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.admin.application.AdminManagementService;
import com.souzip.api.domain.admin.application.AdminManagementService.AdminPageResult;
import com.souzip.api.domain.admin.fixture.TestAdminPasswordEncoder;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.presentation.request.InviteAdminRequest;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @DisplayName("관리자 초대 - ADMIN 역할")
    @Test
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
                        .description("아이디 (2-20자, 영문/숫자/언더스코어/한글)"),
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

    @DisplayName("관리자 목록 조회")
    @Test
    void getAdmins_success() throws Exception {
        // given
        setSuperAdminAuthentication();

        List<Admin> admins = List.of(
            Admin.create("admin1", "password123", AdminRole.ADMIN, new TestAdminPasswordEncoder()),
            Admin.create("admin2", "password123", AdminRole.VIEWER, new TestAdminPasswordEncoder())
        );

        AdminPageResult pageResult = new AdminPageResult(admins, 1, 10, 2, 1);

        given(adminManagementService.getAdmins(anyInt(), anyInt())).willReturn(pageResult);

        // when & then
        mockMvc.perform(get("/api/admin/list")
                .header("Authorization", "Bearer super-admin-token")
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
            .andExpect(jsonPath("$.data.pagination.totalPages").value(1))
            .andExpect(jsonPath("$.data.pagination.totalItems").value(2))
            .andDo(document("admin/get-admins",
                getDocumentRequest(),
                getDocumentResponse(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer {accessToken} - SUPER_ADMIN 권한 필요")
                ),
                queryParameters(
                    parameterWithName("pageNo").description("페이지 번호 (기본값: 1)").optional(),
                    parameterWithName("pageSize").description("페이지 크기 (기본값: 10, 최대: 30)").optional()
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                    fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("관리자 목록"),
                    fieldWithPath("data.content[].id").type(JsonFieldType.STRING).description("관리자 ID"),
                    fieldWithPath("data.content[].username").type(JsonFieldType.STRING).description("아이디"),
                    fieldWithPath("data.content[].role").type(JsonFieldType.STRING).description("역할"),
                    fieldWithPath("data.content[].lastLoginAt").type(JsonFieldType.STRING).description("마지막 로그인 시각")
                        .optional(),
                    fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING).description("생성 시각"),
                    fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이징 정보"),
                    fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지"),
                    fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                    fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 항목 수"),
                    fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                    fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 페이지 여부"),
                    fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                    fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                    fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN)
                        .description("이전 페이지 존재 여부"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));

        SecurityContextHolder.clearContext();
    }

    @DisplayName("관리자 삭제 성공")
    @Test
    void deleteAdmin_success() throws Exception {
        // given
        Admin superAdmin = Admin.create("superadmin", "password", AdminRole.SUPER_ADMIN,
            new TestAdminPasswordEncoder());
        setSuperAdminAuthenticationWithAdmin(superAdmin);

        UUID adminIdToDelete = UUID.randomUUID();

        doNothing().when(adminManagementService).deleteAdmin(any(UUID.class), any(UUID.class));

        // when & then
        mockMvc.perform(delete("/api/admin/{adminId}", adminIdToDelete)
                .header("Authorization", "Bearer super-admin-token"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("관리자가 삭제되었습니다."))
            .andDo(document("admin/delete-admin",
                getDocumentRequest(),
                getDocumentResponse(),
                requestHeaders(
                    headerWithName("Authorization").description("Bearer {accessToken} - SUPER_ADMIN 권한 필요")
                ),
                pathParameters(
                    parameterWithName("adminId").description("삭제할 관리자 ID (UUID)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)").optional(),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                )
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

    private void setSuperAdminAuthenticationWithAdmin(Admin admin) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                admin,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
