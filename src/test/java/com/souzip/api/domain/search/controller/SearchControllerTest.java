package com.souzip.api.domain.search.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.search.dto.SearchResponse;
import com.souzip.api.domain.search.service.SearchService;
import com.souzip.api.global.common.dto.pagination.PaginationRequest;
import com.souzip.api.global.common.dto.pagination.PaginationResponse;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.math.BigDecimal;
import java.util.List;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static com.souzip.api.docs.CommonDocumentation.errorResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchControllerTest extends RestDocsSupport {

    private final SearchService searchService = mock(SearchService.class);

    @Override
    protected Object initController() {
        return new SearchController(searchService);
    }

    private PaginationResponse<SearchResponse> createMockPaginationResponse(
            List<SearchResponse> content,
            int currentPage, int totalPages, long totalItems, int pageSize,
            boolean first, boolean last, boolean hasNext, boolean hasPrevious
    ) {
        PaginationResponse<SearchResponse> response = mock(PaginationResponse.class);
        PaginationResponse.PageInfo pageInfo = mock(PaginationResponse.PageInfo.class);

        given(response.getContent()).willReturn(content);
        given(response.getPagination()).willReturn(pageInfo);
        given(pageInfo.getCurrentPage()).willReturn(currentPage);
        given(pageInfo.getTotalPages()).willReturn(totalPages);
        given(pageInfo.getTotalItems()).willReturn(totalItems);
        given(pageInfo.getPageSize()).willReturn(pageSize);
        given(pageInfo.isFirst()).willReturn(first);
        given(pageInfo.isLast()).willReturn(last);
        given(pageInfo.isHasNext()).willReturn(hasNext);
        given(pageInfo.isHasPrevious()).willReturn(hasPrevious);

        return response;
    }

    @DisplayName("도시명으로 위치를 검색한다 (정확한 일치)")
    @Test
    void searchLocations_exactCityMatch() throws Exception {
        // given
        SearchResponse seoul = new SearchResponse(
                1L, "city", "서울", "Seoul", "서울",
                "대한민국", "South Korea", "대한민국",
                BigDecimal.valueOf(37.56), BigDecimal.valueOf(126.97),
                1.0,      // score
                "서울"    // highlight
        );

        PaginationResponse<SearchResponse> response = createMockPaginationResponse(
                List.of(seoul), 1, 1, 1L, 10, true, true, false, false
        );

        given(searchService.search(eq("서울"), any(PaginationRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/search/locations")
                        .param("keyword", "서울")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].type").value("city"))
                .andExpect(jsonPath("$.data.content[0].nameKr").value("서울"))
                .andDo(document("search/locations-exact-city",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드 (도시명 정확히 일치)"),
                                parameterWithName("pageNo").description("페이지 번호 (1부터 시작)").optional(),
                                parameterWithName("pageSize").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("검색 결과 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("검색된 위치 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("위치 엔티티 ID"),
                                fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("위치 타입 (city)"),
                                fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("위치 이름 (한글)"),
                                fieldWithPath("data.content[].nameEn").type(JsonFieldType.STRING).description("위치 영문 이름"),
                                fieldWithPath("data.content[].nameKr").type(JsonFieldType.STRING).description("위치 한글 이름"),
                                fieldWithPath("data.content[].countryName").type(JsonFieldType.STRING).description("국가 이름 (한글)").optional(),
                                fieldWithPath("data.content[].countryNameEn").type(JsonFieldType.STRING).description("국가 영문 이름").optional(),
                                fieldWithPath("data.content[].countryNameKr").type(JsonFieldType.STRING).description("국가 한글 이름").optional(),
                                fieldWithPath("data.content[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data.content[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("data.content[].score").type(JsonFieldType.NUMBER).description("검색 점수"),
                                fieldWithPath("data.content[].highlight").type(JsonFieldType.STRING).description("하이라이팅된 이름"),
                                fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이지네이션 정보"),
                                fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 결과 개수"),
                                fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                                fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("국가명으로 검색하면 해당 국가의 도시들이 반환된다 (priority 순)")
    @Test
    void searchLocations_countryMatch() throws Exception {
        // given
        SearchResponse tokyo = new SearchResponse(
                1L, "city", "도쿄", "Tokyo", "도쿄",
                "일본", "Japan", "일본",
                BigDecimal.valueOf(35.68), BigDecimal.valueOf(139.69),
                0.8,      // score
                "도쿄"    // highlight
        );

        SearchResponse osaka = new SearchResponse(
                2L, "city", "오사카", "Osaka", "오사카",
                "일본", "Japan", "일본",
                BigDecimal.valueOf(34.69), BigDecimal.valueOf(135.50),
                0.8,      // score
                "오사카"  // highlight
        );

        PaginationResponse<SearchResponse> response = createMockPaginationResponse(
                List.of(tokyo, osaka), 1, 1, 2L, 10, true, true, false, false
        );

        given(searchService.search(eq("일본"), any(PaginationRequest.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/search/locations")
                        .param("keyword", "일본")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].type").value("city"))
                .andExpect(jsonPath("$.data.content[0].countryNameKr").value("일본"))
                .andExpect(jsonPath("$.data.content[1].type").value("city"))
                .andExpect(jsonPath("$.data.content[1].countryNameKr").value("일본"))
                .andDo(document("search/locations-country",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드 (국가명 정확히 일치)"),
                                parameterWithName("pageNo").description("페이지 번호 (1부터 시작)").optional(),
                                parameterWithName("pageSize").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("검색 결과 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("검색된 도시 목록 (priority 순으로 정렬)"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("위치 엔티티 ID"),
                                fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("위치 타입 (city)"),
                                fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("도시 이름 (한글)"),
                                fieldWithPath("data.content[].nameEn").type(JsonFieldType.STRING).description("도시 영문 이름"),
                                fieldWithPath("data.content[].nameKr").type(JsonFieldType.STRING).description("도시 한글 이름"),
                                fieldWithPath("data.content[].countryName").type(JsonFieldType.STRING).description("국가 이름 (한글)").optional(),
                                fieldWithPath("data.content[].countryNameEn").type(JsonFieldType.STRING).description("국가 영문 이름").optional(),
                                fieldWithPath("data.content[].countryNameKr").type(JsonFieldType.STRING).description("국가 한글 이름").optional(),
                                fieldWithPath("data.content[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data.content[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("data.content[].score").type(JsonFieldType.NUMBER).description("검색 점수"),
                                fieldWithPath("data.content[].highlight").type(JsonFieldType.STRING).description("하이라이팅된 이름"),
                                fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이지네이션 정보"),
                                fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 결과 개수"),
                                fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                                fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("검색어가 비어있으면 400 에러가 발생한다")
    @Test
    void searchLocations_emptyKeyword() throws Exception {
        // given
        given(searchService.search(eq(""), any(PaginationRequest.class)))
                .willThrow(new BusinessException(ErrorCode.INVALID_INPUT, "검색어를 입력해주세요."));

        mockMvc.perform(get("/api/search/locations")
                        .param("keyword", "")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("검색어를 입력해주세요."))
                .andDo(document("search/locations-empty-keyword",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("빈 검색 키워드"),
                                parameterWithName("pageNo").description("페이지 번호 (1부터 시작)").optional(),
                                parameterWithName("pageSize").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(errorResponseFields())
                ));
    }

    @DisplayName("검색 결과가 없으면 빈 목록을 반환한다")
    @Test
    void searchLocations_noResults() throws Exception {
        // given
        PaginationResponse<SearchResponse> response = createMockPaginationResponse(
                List.of(), 1, 0, 0L, 10, true, true, false, false
        );

        given(searchService.search(eq("존재하지않는도시"), any(PaginationRequest.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/search/locations")
                        .param("keyword", "존재하지않는도시")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.pagination.totalItems").value(0))
                .andDo(document("search/locations-no-results",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("존재하지 않는 검색 키워드"),
                                parameterWithName("pageNo").description("페이지 번호 (1부터 시작)").optional(),
                                parameterWithName("pageSize").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("검색 결과 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("빈 결과 목록"),
                                fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이지네이션 정보"),
                                fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수 (0)"),
                                fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 결과 개수 (0)"),
                                fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                                fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("부분 일치는 검색되지 않는다")
    @Test
    void searchLocations_partialMatchNotFound() throws Exception {
        // given
        PaginationResponse<SearchResponse> response = createMockPaginationResponse(
                List.of(), 1, 0, 0L, 10, true, true, false, false
        );

        given(searchService.search(eq("서"), any(PaginationRequest.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/search/locations")
                        .param("keyword", "서")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andDo(document("search/locations-partial-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("부분 일치 키워드 (정확한 이름만 검색됨)"),
                                parameterWithName("pageNo").description("페이지 번호 (1부터 시작)").optional(),
                                parameterWithName("pageSize").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("검색 결과 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("빈 결과 목록 (부분 일치는 검색 안됨)"),
                                fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이지네이션 정보"),
                                fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 결과 개수"),
                                fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                                fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("공백만 입력하면 400 에러가 발생한다")
    @Test
    void searchLocations_blankKeyword() throws Exception {
        // given
        given(searchService.search(eq("   "), any(PaginationRequest.class)))
                .willThrow(new BusinessException(ErrorCode.INVALID_INPUT, "검색어를 입력해주세요."));

        // when & then
        mockMvc.perform(get("/api/search/locations")
                        .param("keyword", "   ")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("검색어를 입력해주세요."))
                .andDo(document("search/locations-blank-keyword",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("공백으로만 이루어진 검색 키워드"),
                                parameterWithName("pageNo").description("페이지 번호 (1부터 시작)").optional(),
                                parameterWithName("pageSize").description("페이지 크기 (기본값: 10)").optional()
                        ),
                        responseFields(errorResponseFields())
                ));
    }

    @DisplayName("페이지네이션으로 결과를 나누어 조회한다")
    @Test
    void searchLocations_pagination() throws Exception {
        // given
        List<SearchResponse> cities = List.of(
                new SearchResponse(1L, "city", "도쿄", "Tokyo", "도쿄",
                        "일본", "Japan", "일본",
                        BigDecimal.valueOf(35.68), BigDecimal.valueOf(139.69),
                        0.8, "도쿄"),
                new SearchResponse(2L, "city", "오사카", "Osaka", "오사카",
                        "일본", "Japan", "일본",
                        BigDecimal.valueOf(34.69), BigDecimal.valueOf(135.50),
                        0.8, "오사카")
        );

        PaginationResponse<SearchResponse> response = createMockPaginationResponse(
                cities, 1, 3, 25L, 10, true, false, true, false
        );

        given(searchService.search(eq("일본"), any(PaginationRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/search/locations")
                        .param("keyword", "일본")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.totalPages").value(3))
                .andExpect(jsonPath("$.data.pagination.totalItems").value(25))
                .andDo(document("search/locations-pagination",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드"),
                                parameterWithName("pageNo").description("페이지 번호 (1부터 시작)"),
                                parameterWithName("pageSize").description("페이지 크기 (기본값: 10)")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("검색 결과 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("검색된 위치 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("위치 엔티티 ID"),
                                fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("위치 타입"),
                                fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("위치 이름 (한글)"),
                                fieldWithPath("data.content[].nameEn").type(JsonFieldType.STRING).description("위치 영문 이름"),
                                fieldWithPath("data.content[].nameKr").type(JsonFieldType.STRING).description("위치 한글 이름"),
                                fieldWithPath("data.content[].countryName").type(JsonFieldType.STRING).description("국가 이름 (한글)").optional(),
                                fieldWithPath("data.content[].countryNameEn").type(JsonFieldType.STRING).description("국가 영문 이름").optional(),
                                fieldWithPath("data.content[].countryNameKr").type(JsonFieldType.STRING).description("국가 한글 이름").optional(),
                                fieldWithPath("data.content[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data.content[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("data.content[].score").type(JsonFieldType.NUMBER).description("검색 점수"),
                                fieldWithPath("data.content[].highlight").type(JsonFieldType.STRING).description("하이라이팅된 이름"),
                                fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이지네이션 정보"),
                                fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 결과 개수"),
                                fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                                fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }
}
