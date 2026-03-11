package com.souzip.adapter.webapi.admin;

import com.souzip.application.admin.provided.AdminFinder;
import com.souzip.application.admin.provided.AdminModifier;
import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.AdminFixture;
import com.souzip.domain.admin.AdminRegisterRequest;
import com.souzip.domain.admin.AdminRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminApiTest extends RestDocsSupport {

    private final AdminFinder adminFinder = mock(AdminFinder.class);
    private final AdminModifier adminModifier = mock(AdminModifier.class);

    @Override
    protected Object initController() {
        return new AdminApi(adminFinder, adminModifier);
    }

    @DisplayName("어드민을 등록한다")
    @Test
    void register() throws Exception {
        Admin admin = AdminFixture.createAdmin();
        given(adminModifier.register(any(AdminRegisterRequest.class))).willReturn(admin);

        AdminRegisterRequest request = AdminRegisterRequest.of("admin123", "password123", AdminRole.ADMIN);

        mockMvc.perform(post("/api/admin/register")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin123"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andDo(document("admin/invite-admin",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("username").type(JsonFieldType.STRING).description("아이디 (2~20자)"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호 (8자 이상)"),
                                fieldWithPath("role").type(JsonFieldType.STRING).description("역할 (ADMIN, VIEWER)")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.adminId").type(JsonFieldType.STRING).description("어드민 ID"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("아이디"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING).description("역할"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    @DisplayName("어드민 목록을 조회한다")
    @Test
    void getAdmins() throws Exception {
        List<Admin> admins = List.of(
                AdminFixture.createAdmin("admin1"),
                AdminFixture.createAdmin("admin2")
        );
        Page<Admin> page = new PageImpl<>(admins, PageRequest.of(0, 10), 2);
        given(adminFinder.findAll(any())).willReturn(page);

        mockMvc.perform(get("/api/admin")
                        .header("Authorization", "Bearer access-token")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andDo(document("admin/get-admins",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        queryParameters(
                                parameterWithName("pageNo").description("페이지 번호 (1부터 시작)").optional(),
                                parameterWithName("pageSize").description("페이지 크기 (기본 10, 최대 30)").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.content[]").type(JsonFieldType.ARRAY).description("어드민 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.STRING).description("어드민 ID"),
                                fieldWithPath("data.content[].username").type(JsonFieldType.STRING).description("아이디"),
                                fieldWithPath("data.content[].role").type(JsonFieldType.STRING).description("역할"),
                                fieldWithPath("data.content[].lastLoginAt").type(JsonFieldType.STRING).description("마지막 로그인 시간").optional(),
                                fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이지 정보"),
                                fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지"),
                                fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 수"),
                                fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 페이지 여부"),
                                fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부"),
                                fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("어드민을 삭제한다")
    @Test
    void deleteAdmin() throws Exception {
        willDoNothing().given(adminModifier).delete(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/admin/{adminId}", UUID.randomUUID())
                        .header("Authorization", "Bearer access-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("관리자가 삭제되었습니다."))
                .andDo(document("admin/delete-admin",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("adminId").description("삭제할 어드민 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }
}