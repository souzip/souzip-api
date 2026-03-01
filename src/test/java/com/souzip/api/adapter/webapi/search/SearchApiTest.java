package com.souzip.api.adapter.webapi.search;

import com.souzip.api.application.search.dto.CitySearchResult;
import com.souzip.api.application.search.dto.PlaceSearchResult;
import com.souzip.api.application.search.dto.SearchPlace;
import com.souzip.api.application.search.provided.LocationSearch;
import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.entity.Region;
import com.souzip.api.domain.shared.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.math.BigDecimal;
import java.util.List;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchApiTest extends RestDocsSupport {

    private final LocationSearch locationSearch = mock(LocationSearch.class);

    @Override
    protected Object initController() {
        return new SearchApi(locationSearch);
    }

    @DisplayName("도시를 검색할 수 있다")
    @Test
    void searchCity() throws Exception {
        // given
        String keyword = "파리";
        City city = createCity("파리", "Paris");
        CitySearchResult result = new CitySearchResult(List.of(city));

        given(locationSearch.search(anyString()))
                .willReturn(result);

        // when & then
        mockMvc.perform(get("/api/search")
                        .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].type").value("city"))
                .andExpect(jsonPath("$.data[0].name").value("파리"))
                .andExpect(jsonPath("$.data[0].country").value("프랑스"))
                .andExpect(jsonPath("$.data[0].address").doesNotExist())
                .andExpect(jsonPath("$.data[0].latitude").value(48.8566))
                .andExpect(jsonPath("$.data[0].longitude").value(2.3522))
                .andDo(document("search/search-city",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("검색어 (도시명 또는 국가명)")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("검색 결과 목록"),
                                fieldWithPath("data[].type").type(JsonFieldType.STRING).description("결과 타입 (city, place)"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("data[].country").type(JsonFieldType.STRING).description("국가명 (City인 경우)").optional(),
                                fieldWithPath("data[].address").type(JsonFieldType.STRING).description("주소 (Place인 경우)").optional(),
                                fieldWithPath("data[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("장소를 검색할 수 있다")
    @Test
    void searchPlace() throws Exception {
        // given
        String keyword = "파리 에펠탑";
        SearchPlace place = createPlace(
                "에펠탑 기념품샵",
                "프랑스 파리 샹드마르스 에펠탑",
                48.8584,
                2.2945
        );
        PlaceSearchResult result = new PlaceSearchResult(List.of(place));

        given(locationSearch.search(anyString()))
                .willReturn(result);

        // when & then
        mockMvc.perform(get("/api/search")
                        .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].type").value("place"))
                .andExpect(jsonPath("$.data[0].name").value("에펠탑 기념품샵"))
                .andExpect(jsonPath("$.data[0].country").doesNotExist())
                .andExpect(jsonPath("$.data[0].address").value("프랑스 파리 샹드마르스 에펠탑"))
                .andExpect(jsonPath("$.data[0].latitude").value(48.8584))
                .andExpect(jsonPath("$.data[0].longitude").value(2.2945))
                .andDo(document("search/search-place",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("검색어 (장소명)")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("검색 결과 목록"),
                                fieldWithPath("data[].type").type(JsonFieldType.STRING).description("결과 타입 (city, place)"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING).description("장소명"),
                                fieldWithPath("data[].country").type(JsonFieldType.STRING).description("국가명 (City인 경우)").optional(),
                                fieldWithPath("data[].address").type(JsonFieldType.STRING).description("주소 (Place인 경우)").optional(),
                                fieldWithPath("data[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("검색 결과가 없는 경우 빈 배열을 반환한다")
    @Test
    void searchEmptyResult() throws Exception {
        // given
        String keyword = "존재하지않는장소12345";
        PlaceSearchResult emptyResult = new PlaceSearchResult(List.of());

        given(locationSearch.search(anyString()))
                .willReturn(emptyResult);

        // when & then
        mockMvc.perform(get("/api/search")
                        .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    private City createCity(String nameKr, String nameEn) {
        Country country = createCountry("프랑스", "France");

        return City.create(
                nameEn,
                nameKr,
                BigDecimal.valueOf(48.8566),
                BigDecimal.valueOf(2.3522),
                country
        );
    }

    private Country createCountry(String nameKr, String nameEn) {
        return Country.of(
                nameEn,
                nameKr,
                "FR",
                "Paris",
                Region.EUROPE,
                "https://example.com/france.png",
                BigDecimal.valueOf(48.8566),
                BigDecimal.valueOf(2.3522),
                null
        );
    }

    private SearchPlace createPlace(String name, String address, double lat, double lng) {
        return new SearchPlace(
                name,
                address,
                Coordinate.of(
                        BigDecimal.valueOf(lat),
                        BigDecimal.valueOf(lng)
                )
        );
    }
}
