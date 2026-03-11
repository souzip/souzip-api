package com.souzip.adapter.webapi.admin;

import com.souzip.application.admin.provided.AdminLocationFinder;
import com.souzip.application.admin.provided.AdminLocationModifier;
import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.city.entity.City;
import com.souzip.domain.city.entity.CityCreateRequest;
import com.souzip.domain.city.entity.CityUpdateRequest;
import com.souzip.domain.country.entity.Country;
import com.souzip.domain.shared.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.math.BigDecimal;
import java.util.List;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.*;
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

class AdminLocationApiTest extends RestDocsSupport {

    private final AdminLocationFinder adminLocationFinder = mock(AdminLocationFinder.class);
    private final AdminLocationModifier adminLocationModifier = mock(AdminLocationModifier.class);

    @Override
    protected Object initController() {
        return new AdminLocationApi(adminLocationFinder, adminLocationModifier);
    }

    @DisplayName("나라 목록을 조회한다")
    @Test
    void getCountries() throws Exception {
        Country country = createCountry(1L, "대한민국");
        given(adminLocationFinder.getCountries(any())).willReturn(List.of(country));

        mockMvc.perform(get("/api/admin/countries")
                        .header("Authorization", "Bearer access-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].nameKr").value("대한민국"))
                .andDo(document("admin/get-countries",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        apiResponseFields(
                                fieldWithPath("data[]").type(JsonFieldType.ARRAY).description("나라 목록"),
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("나라 ID"),
                                fieldWithPath("data[].nameKr").type(JsonFieldType.STRING).description("나라 한글명"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("도시 목록을 조회한다")
    @Test
    void getCities() throws Exception {
        City city = createCity(1L, "Seoul", "서울");
        Page<City> page = new PageImpl<>(List.of(city), PageRequest.of(0, 10), 1);
        given(adminLocationFinder.getCities(anyLong(), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/admin/cities")
                        .header("Authorization", "Bearer access-token")
                        .param("countryId", "1")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("admin/get-cities",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        queryParameters(
                                parameterWithName("countryId").description("나라 ID"),
                                parameterWithName("keyword").description("검색어 (한글명/영문명)").optional(),
                                parameterWithName("pageNo").description("페이지 번호 (1부터 시작)").optional(),
                                parameterWithName("pageSize").description("페이지 크기 (기본 10, 최대 30)").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.content[]").type(JsonFieldType.ARRAY).description("도시 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("도시 ID"),
                                fieldWithPath("data.content[].nameEn").type(JsonFieldType.STRING).description("영문명"),
                                fieldWithPath("data.content[].nameKr").type(JsonFieldType.STRING).description("한글명"),
                                fieldWithPath("data.content[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data.content[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("data.content[].priority").type(JsonFieldType.NUMBER).description("우선순위").optional(),
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

    @DisplayName("도시를 추가한다")
    @Test
    void createCity() throws Exception {
        willDoNothing().given(adminLocationModifier).createCity(any(CityCreateRequest.class));

        CityCreateRequest request = new CityCreateRequest(
                "Seoul", "서울",
                Coordinate.of(BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780)),
                1L
        );

        mockMvc.perform(post("/api/admin/cities")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("도시가 추가되었습니다."))
                .andDo(document("admin/create-city",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("nameEn").type(JsonFieldType.STRING).description("영문명"),
                                fieldWithPath("nameKr").type(JsonFieldType.STRING).description("한글명"),
                                fieldWithPath("coordinate.latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("coordinate.longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("countryId").type(JsonFieldType.NUMBER).description("나라 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    @DisplayName("도시 정보를 수정한다")
    @Test
    void updateCity() throws Exception {
        willDoNothing().given(adminLocationModifier).updateCity(anyLong(), any(CityUpdateRequest.class));

        CityUpdateRequest request = new CityUpdateRequest(
                "Seoul", "서울",
                Coordinate.of(BigDecimal.valueOf(37.5665), BigDecimal.valueOf(126.9780))
        );

        mockMvc.perform(patch("/api/admin/cities/{cityId}", 1L)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("도시 정보가 수정되었습니다."))
                .andDo(document("admin/update-city",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("cityId").description("도시 ID")
                        ),
                        requestFields(
                                fieldWithPath("nameEn").type(JsonFieldType.STRING).description("영문명"),
                                fieldWithPath("nameKr").type(JsonFieldType.STRING).description("한글명"),
                                fieldWithPath("coordinate.latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("coordinate.longitude").type(JsonFieldType.NUMBER).description("경도")
                        ),
                        apiResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    @DisplayName("도시를 삭제한다")
    @Test
    void deleteCity() throws Exception {
        willDoNothing().given(adminLocationModifier).deleteCity(anyLong());

        mockMvc.perform(delete("/api/admin/cities/{cityId}", 1L)
                        .header("Authorization", "Bearer access-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("도시가 삭제되었습니다."))
                .andDo(document("admin/delete-city",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("cityId").description("도시 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    @DisplayName("도시 우선순위를 설정한다")
    @Test
    void updateCityPriority() throws Exception {
        willDoNothing().given(adminLocationModifier).updateCityPriority(anyLong(), anyInt());

        mockMvc.perform(patch("/api/admin/cities/{cityId}/priority", 1L)
                        .header("Authorization", "Bearer access-token")
                        .param("priority", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("우선순위가 업데이트되었습니다."))
                .andDo(document("admin/update-city-priority",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer 액세스 토큰")
                        ),
                        pathParameters(
                                parameterWithName("cityId").description("도시 ID")
                        ),
                        queryParameters(
                                parameterWithName("priority").description("우선순위 (미입력 시 초기화)").optional()
                        ),
                        apiResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    private Country createCountry(Long id, String nameKr) {
        Country country = mock(Country.class);

        given(country.getId()).willReturn(id);
        given(country.getNameKr()).willReturn(nameKr);

        return country;
    }

    private City createCity(Long id, String nameEn, String nameKr) {
        City city = mock(City.class);

        given(city.getId()).willReturn(id);
        given(city.getNameEn()).willReturn(nameEn);
        given(city.getNameKr()).willReturn(nameKr);
        given(city.getLatitude()).willReturn(BigDecimal.valueOf(37.5665));
        given(city.getLongitude()).willReturn(BigDecimal.valueOf(126.9780));
        given(city.getPriority()).willReturn(null);

        return city;
    }
}