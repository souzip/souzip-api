package com.souzip.domain.admin.presentation;

import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.admin.application.AdminCityQueryUseCase;
import com.souzip.domain.admin.application.AdminCountryQueryUseCase;
import com.souzip.domain.admin.application.AdminManagementService;
import com.souzip.domain.admin.application.AdminManagementService.AdminPageResult;
import com.souzip.domain.admin.application.command.AdminCreateCityCommand;
import com.souzip.domain.admin.application.command.AdminDeleteCityCommand;
import com.souzip.domain.admin.application.command.AdminUpdateCityCommand;
import com.souzip.domain.admin.application.command.AdminUpdateCityPriorityCommand;
import com.souzip.domain.admin.application.command.InviteAdminCommand;
import com.souzip.domain.admin.application.port.CityQueryPort.CityQueryResult;
import com.souzip.domain.admin.application.port.CountryQueryPort.CountryQueryResult;
import com.souzip.domain.admin.application.query.CitySearchQuery;
import com.souzip.domain.admin.fixture.TestAdminPasswordEncoder;
import com.souzip.domain.admin.model.Admin;
import com.souzip.domain.admin.model.AdminRole;
import com.souzip.domain.admin.presentation.request.CreateCityRequest;
import com.souzip.domain.admin.presentation.request.InviteAdminRequest;
import com.souzip.domain.admin.presentation.request.UpdateCityRequest;
import com.souzip.global.common.dto.pagination.PaginationResponse;
import java.time.LocalDateTime;
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

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
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
    private final AdminCityQueryUseCase adminCityQueryUseCase = mock(AdminCityQueryUseCase.class);
    private final AdminCountryQueryUseCase adminCountryQueryUseCase = mock(AdminCountryQueryUseCase.class);

    @Override
    protected Object initController() {
        return new AdminManagementController(adminManagementService, adminCityQueryUseCase, adminCountryQueryUseCase);
    }

    @DisplayName("관리자 초대 - ADMIN 역할")
    @Test
    void inviteAdmin_withAdminRole_success() throws Exception {
        setSuperAdminAuthentication();

        InviteAdminRequest request = new InviteAdminRequest(
                "newadmin",
                "password123",
                AdminRole.ADMIN
        );

        Admin createdAdmin = Admin.create("newadmin", "password123", AdminRole.ADMIN,
                new TestAdminPasswordEncoder());

        given(adminManagementService.inviteAdmin(
                new InviteAdminCommand(
                        request.username(),
                        request.password(),
                        request.role()
                )
        )).willReturn(createdAdmin);

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
        setSuperAdminAuthentication();

        List<Admin> admins = List.of(
                Admin.create("admin1", "password123", AdminRole.ADMIN, new TestAdminPasswordEncoder()),
                Admin.create("admin2", "password123", AdminRole.VIEWER, new TestAdminPasswordEncoder())
        );

        AdminPageResult pageResult = new AdminPageResult(admins, 1, 10, 2, 1);

        given(adminManagementService.getAdmins(anyInt(), anyInt())).willReturn(pageResult);

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
                                fieldWithPath("data.content[].lastLoginAt").type(JsonFieldType.STRING).description("마지막 로그인 시각").optional(),
                                fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING).description("생성 시각"),
                                fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이징 정보"),
                                fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지"),
                                fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 항목 수"),
                                fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 페이지 여부"),
                                fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));

        SecurityContextHolder.clearContext();
    }

    @DisplayName("관리자 삭제 성공")
    @Test
    void deleteAdmin_success() throws Exception {
        Admin superAdmin = Admin.create("superadmin", "password", AdminRole.SUPER_ADMIN,
                new TestAdminPasswordEncoder());
        setSuperAdminAuthenticationWithAdmin(superAdmin);

        UUID adminIdToDelete = UUID.randomUUID();

        doNothing().when(adminManagementService).deleteAdmin(any(UUID.class), any(UUID.class));

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

    @DisplayName("나라 목록 조회 성공")
    @Test
    void getCountries_success() throws Exception {
        setAdminAuthentication();

        List<CountryQueryResult> countries = List.of(
                new CountryQueryResult(1L, "대한민국"),
                new CountryQueryResult(2L, "일본")
        );

        given(adminCountryQueryUseCase.getCountries(null)).willReturn(countries);

        mockMvc.perform(get("/api/admin/countries")
                        .header("Authorization", "Bearer admin-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].nameKr").value("대한민국"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].nameKr").value("일본"))
                .andDo(document("admin/get-countries",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer {accessToken} - SUPER_ADMIN 또는 ADMIN 또는 VIEWER 권한 필요")
                        ),
                        queryParameters(
                                parameterWithName("keyword").description("국가명 검색 키워드 (한글명)").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("나라 목록"),
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("나라 ID"),
                                fieldWithPath("data[].nameKr").type(JsonFieldType.STRING).description("나라 한글 이름"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));

        SecurityContextHolder.clearContext();
    }

    @DisplayName("나라 키워드 검색 성공")
    @Test
    void getCountries_withKeyword_success() throws Exception {
        setAdminAuthentication();

        List<CountryQueryResult> countries = List.of(
                new CountryQueryResult(1L, "대한민국")
        );

        given(adminCountryQueryUseCase.getCountries("한국")).willReturn(countries);

        mockMvc.perform(get("/api/admin/countries")
                        .header("Authorization", "Bearer admin-token")
                        .param("keyword", "한국"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nameKr").value("대한민국"))
                .andExpect(jsonPath("$.data.length()").value(1));

        SecurityContextHolder.clearContext();
    }

    @DisplayName("도시 목록 조회 성공")
    @Test
    void getCities_success() throws Exception {
        setAdminAuthentication();
        LocalDateTime now = LocalDateTime.now();

        List<CityQueryResult> content = List.of(
                new CityQueryResult(1L, "서울", "Seoul", 1, now),
                new CityQueryResult(2L, "부산", "Busan", 2, now)
        );

        PaginationResponse<CityQueryResult> pageResponse = PaginationResponse.of(
                content, 1, 20, 2, 1
        );

        given(adminCityQueryUseCase.getCities(any(CitySearchQuery.class))).willReturn(pageResponse);

        mockMvc.perform(get("/api/admin/cities")
                        .header("Authorization", "Bearer admin-token")
                        .param("countryId", "83")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].nameKr").value("서울"))
                .andExpect(jsonPath("$.data.content[0].nameEn").value("Seoul"))
                .andExpect(jsonPath("$.data.content[0].priority").value(1))
                .andExpect(jsonPath("$.data.content[1].id").value(2))
                .andExpect(jsonPath("$.data.content[1].nameKr").value("부산"))
                .andExpect(jsonPath("$.data.content[1].nameEn").value("Busan"))
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.totalItems").value(2))
                .andDo(document("admin/get-cities",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer {accessToken} - SUPER_ADMIN 또는 ADMIN 또는 VIEWER 권한 필요")
                        ),
                        queryParameters(
                                parameterWithName("countryId").description("나라 ID (기본값: 83)").optional(),
                                parameterWithName("keyword").description("도시명 검색어 (한글명, 영문명)").optional(),
                                parameterWithName("pageNo").description("페이지 번호 (기본값: 1)").optional(),
                                parameterWithName("pageSize").description("페이지 크기 (기본값: 10, 최대: 30)").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("도시 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("도시 ID"),
                                fieldWithPath("data.content[].nameKr").type(JsonFieldType.STRING).description("도시 한글 이름"),
                                fieldWithPath("data.content[].nameEn").type(JsonFieldType.STRING).description("도시 영문 이름"),
                                fieldWithPath("data.content[].priority").type(JsonFieldType.NUMBER).description("우선순위").optional(),
                                fieldWithPath("data.content[].updatedAt").type(JsonFieldType.STRING).description("수정 시각"),
                                fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이징 정보"),
                                fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지"),
                                fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 항목 수"),
                                fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 페이지 여부"),
                                fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));

        SecurityContextHolder.clearContext();
    }

    @DisplayName("도시 키워드 검색 성공")
    @Test
    void getCities_withKeyword_success() throws Exception {
        setAdminAuthentication();
        LocalDateTime now = LocalDateTime.now();

        List<CityQueryResult> content = List.of(
                new CityQueryResult(1L, "서울", "Seoul", 1, now)
        );

        PaginationResponse<CityQueryResult> pageResponse = PaginationResponse.of(
                content, 1, 20, 1, 1
        );

        given(adminCityQueryUseCase.getCities(any(CitySearchQuery.class))).willReturn(pageResponse);

        mockMvc.perform(get("/api/admin/cities")
                        .header("Authorization", "Bearer admin-token")
                        .param("countryId", "83")
                        .param("keyword", "서울")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].nameKr").value("서울"))
                .andExpect(jsonPath("$.data.content[0].nameEn").value("Seoul"))
                .andExpect(jsonPath("$.data.pagination.totalItems").value(1));

        SecurityContextHolder.clearContext();
    }

    @DisplayName("도시 추가 성공")
    @Test
    void createCity_success() throws Exception {
        setAdminAuthentication();

        CreateCityRequest request = new CreateCityRequest(
                "Seoul", "서울", 37.56, 126.97, 1L
        );

        doNothing().when(adminManagementService).createCity(
                new AdminCreateCityCommand(
                        request.nameEn(),
                        request.nameKr(),
                        request.latitude(),
                        request.longitude(),
                        request.countryId()
                )
        );

        mockMvc.perform(post("/api/admin/cities")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("도시가 추가되었습니다."))
                .andDo(document("admin/create-city",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer {accessToken} - SUPER_ADMIN 또는 ADMIN 권한 필요")
                        ),
                        requestFields(
                                fieldWithPath("nameEn").type(JsonFieldType.STRING).description("도시 영문명"),
                                fieldWithPath("nameKr").type(JsonFieldType.STRING).description("도시 한글명"),
                                fieldWithPath("latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("countryId").type(JsonFieldType.NUMBER).description("나라 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)").optional(),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));

        SecurityContextHolder.clearContext();
    }

    @DisplayName("도시 이름 수정 성공")
    @Test
    void updateCityName_success() throws Exception {
        setAdminAuthentication();
        Long cityId = 1L;

        UpdateCityRequest request = new UpdateCityRequest(
                "Seoul",
                "서울특별시"
        );

        doNothing().when(adminManagementService).updateCity(
                new AdminUpdateCityCommand(
                        cityId,
                        request.nameEn(),
                        request.nameKr()
                )
        );

        mockMvc.perform(patch("/api/admin/cities/{cityId}/name", cityId)
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("도시 이름이 수정되었습니다."))
                .andDo(document("admin/update-city-name",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer {accessToken} - SUPER_ADMIN 또는 ADMIN 권한 필요")
                        ),
                        pathParameters(
                                parameterWithName("cityId").description("수정할 도시 ID")
                        ),
                        requestFields(
                                fieldWithPath("nameEn").type(JsonFieldType.STRING).description("도시 영문명"),
                                fieldWithPath("nameKr").type(JsonFieldType.STRING).description("도시 한글명")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)").optional(),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));

        SecurityContextHolder.clearContext();
    }

    @DisplayName("도시 삭제 성공")
    @Test
    void deleteCity_success() throws Exception {
        setAdminAuthentication();
        Long cityId = 1L;

        doNothing().when(adminManagementService)
                .deleteCity(new AdminDeleteCityCommand(cityId));

        mockMvc.perform(delete("/api/admin/cities/{cityId}", cityId)
                        .header("Authorization", "Bearer admin-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("도시가 삭제되었습니다."))
                .andDo(document("admin/delete-city",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer {accessToken} - SUPER_ADMIN 또는 ADMIN 권한 필요")
                        ),
                        pathParameters(
                                parameterWithName("cityId").description("삭제할 도시 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)").optional(),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));

        SecurityContextHolder.clearContext();
    }

    @DisplayName("도시 우선순위 설정 성공")
    @Test
    void updateCityPriority_success() throws Exception {
        setAdminAuthentication();
        Long cityId = 1L;

        doNothing().when(adminManagementService)
                .updateCityPriority(new AdminUpdateCityPriorityCommand(cityId, 1));

        mockMvc.perform(patch("/api/admin/cities/{cityId}/priority", cityId)
                        .header("Authorization", "Bearer admin-token")
                        .param("priority", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("우선순위가 업데이트되었습니다."))
                .andDo(document("admin/update-city-priority",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer {accessToken} - SUPER_ADMIN 또는 ADMIN 권한 필요")
                        ),
                        pathParameters(
                                parameterWithName("cityId").description("도시 ID")
                        ),
                        queryParameters(
                                parameterWithName("priority").description("우선순위 (1 이상, 미입력 시 초기화)").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)").optional(),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));

        SecurityContextHolder.clearContext();
    }

    @DisplayName("도시 우선순위 초기화 성공")
    @Test
    void updateCityPriority_reset_success() throws Exception {
        setAdminAuthentication();
        Long cityId = 1L;

        doNothing().when(adminManagementService)
                .updateCityPriority(new AdminUpdateCityPriorityCommand(cityId, null));

        mockMvc.perform(patch("/api/admin/cities/{cityId}/priority", cityId)
                        .header("Authorization", "Bearer admin-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("우선순위가 업데이트되었습니다."))
                .andDo(document("admin/reset-city-priority",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer {accessToken} - SUPER_ADMIN 또는 ADMIN 권한 필요")
                        ),
                        pathParameters(
                                parameterWithName("cityId").description("도시 ID")
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

    private void setAdminAuthentication() {
        Admin admin = Admin.create("admin", "password", AdminRole.ADMIN,
                new TestAdminPasswordEncoder());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        admin,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
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
