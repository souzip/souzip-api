package com.souzip.adapter.webapi.location;

import com.souzip.application.location.dto.AddressResult;
import com.souzip.application.location.dto.CitySearchResult;
import com.souzip.application.location.dto.PlaceSearchResult;
import com.souzip.application.location.dto.SearchPlace;
import com.souzip.application.location.provided.LocationSearch;
import com.souzip.application.location.provided.ReverseGeocoding;
import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.city.entity.City;
import com.souzip.domain.country.entity.Country;
import com.souzip.domain.shared.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.any;
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

class LocationApiTest extends RestDocsSupport {

    private final ReverseGeocoding reverseGeocoding = mock(ReverseGeocoding.class);
    private final LocationSearch locationSearch = mock(LocationSearch.class);

    @Override
    protected Object initController() {
        return new LocationApi(reverseGeocoding, locationSearch);
    }

    @DisplayName("위도와 경도를 통해 주소를 조회할 수 있다")
    @Test
    void getAddress() throws Exception {
        // given
        double latitude = 37.5665121;
        double longitude = 126.9780123;

        AddressResult mockResult = new AddressResult(
                "110 Sejong-daero, Jung District, Seoul, South Korea",
                "Seoul",
                "KR"
        );

        given(reverseGeocoding.getAddress(any(Coordinate.class)))
                .willReturn(mockResult);

        // when & then
        mockMvc.perform(get("/api/location/address")
                        .param("latitude", String.valueOf(latitude))
                        .param("longitude", String.valueOf(longitude)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.address").value("110 Sejong-daero, Jung District, Seoul, South Korea"))
                .andExpect(jsonPath("$.data.city").value("Seoul"))
                .andExpect(jsonPath("$.data.countryCode").value("KR"))
                .andDo(document("location/get-address",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("latitude").description("위도 (decimal, 소수점 7자리까지)"),
                                parameterWithName("longitude").description("경도 (decimal, 소수점 7자리까지)")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.formattedAddress").type(JsonFieldType.STRING)
                                        .description("전체 주소 (Deprecated: address 필드 사용 권장)"),
                                fieldWithPath("data.address").type(JsonFieldType.STRING).description("전체 주소"),
                                fieldWithPath("data.city").type(JsonFieldType.STRING).description("도시 이름"),
                                fieldWithPath("data.countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("주소 정보가 없는 경우 null 값을 반환한다")
    @Test
    void getAddress_EmptyResult() throws Exception {
        // given
        double latitude = 0.0;
        double longitude = 0.0;

        AddressResult emptyResult = AddressResult.empty();

        given(reverseGeocoding.getAddress(any(Coordinate.class)))
                .willReturn(emptyResult);

        // when & then
        mockMvc.perform(get("/api/location/address")
                        .param("latitude", String.valueOf(latitude))
                        .param("longitude", String.valueOf(longitude)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("키워드로 도시를 검색할 수 있다")
    @Test
    void searchCity() throws Exception {
        // given
        String keyword = "서울";

        Country korea = mock(Country.class);
        given(korea.getNameKr()).willReturn("대한민국");

        City seoul = City.create(
                "Seoul",
                "서울",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                korea
        );
        ReflectionTestUtils.setField(seoul, "id", 1L);

        CitySearchResult mockResult = new CitySearchResult(List.of(seoul));

        given(locationSearch.search(anyString()))
                .willReturn(mockResult);

        // when & then
        mockMvc.perform(get("/api/location/search")
                        .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].type").value("city"))
                .andExpect(jsonPath("$.data[0].name").value("서울"))
                .andExpect(jsonPath("$.data[0].country").value("대한민국"))
                .andExpect(jsonPath("$.data[0].coordinate.latitude").value(37.5665))
                .andExpect(jsonPath("$.data[0].coordinate.longitude").value(126.9780))
                .andDo(document("location/search-city",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("검색 결과 목록"),
                                fieldWithPath("data[].type").type(JsonFieldType.STRING).description("검색 결과 타입 (city, place)"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING).description("도시명 또는 장소명"),
                                fieldWithPath("data[].country").type(JsonFieldType.STRING).description("국가명 (도시 검색 시)").optional(),
                                fieldWithPath("data[].address").type(JsonFieldType.STRING).description("주소 (장소 검색 시)").optional(),
                                fieldWithPath("data[].coordinate").type(JsonFieldType.OBJECT).description("좌표"),
                                fieldWithPath("data[].coordinate.latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data[].coordinate.longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("키워드로 장소를 검색할 수 있다 (Google Places)")
    @Test
    void searchPlace() throws Exception {
        // given
        String keyword = "에펠탑";

        SearchPlace eiffelTower = new SearchPlace(
                "Eiffel Tower",
                "Champ de Mars, 5 Avenue Anatole France, 75007 Paris, France",
                "프랑스 파리",
                "tourist_attraction",
                Coordinate.of(BigDecimal.valueOf(48.8584), BigDecimal.valueOf(2.2945))
        );

        PlaceSearchResult mockResult = new PlaceSearchResult(List.of(eiffelTower));

        given(locationSearch.search(anyString()))
                .willReturn(mockResult);

        // when & then
        mockMvc.perform(get("/api/location/search")
                        .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].type").value("place"))
                .andExpect(jsonPath("$.data[0].name").value("Eiffel Tower"))
                .andExpect(jsonPath("$.data[0].address").value("Champ de Mars, 5 Avenue Anatole France, 75007 Paris, France"))
                .andExpect(jsonPath("$.data[0].region").value("프랑스 파리"))
                .andExpect(jsonPath("$.data[0].category").value("tourist_attraction"))
                .andExpect(jsonPath("$.data[0].coordinate.latitude").value(48.8584))
                .andExpect(jsonPath("$.data[0].coordinate.longitude").value(2.2945))
                .andDo(document("location/search-place",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("검색 결과 목록"),
                                fieldWithPath("data[].type").type(JsonFieldType.STRING).description("검색 결과 타입 (city, place)"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING).description("도시명 또는 장소명"),
                                fieldWithPath("data[].country").type(JsonFieldType.STRING).description("국가명 (도시 검색 시)").optional(),
                                fieldWithPath("data[].address").type(JsonFieldType.STRING).description("주소 (장소 검색 시)").optional(),
                                fieldWithPath("data[].region").type(JsonFieldType.STRING).description("지역명 (장소 검색 시)").optional(),
                                fieldWithPath("data[].category").type(JsonFieldType.STRING).description("카테고리 (장소 검색 시)").optional(),
                                fieldWithPath("data[].coordinate").type(JsonFieldType.OBJECT).description("좌표"),
                                fieldWithPath("data[].coordinate.latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data[].coordinate.longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("검색 결과가 없는 경우 빈 배열을 반환한다")
    @Test
    void searchEmpty() throws Exception {
        // given
        String keyword = "존재하지않는장소";

        PlaceSearchResult mockResult = new PlaceSearchResult(List.of());

        given(locationSearch.search(anyString()))
                .willReturn(mockResult);

        // when & then
        mockMvc.perform(get("/api/location/search")
                        .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(document("location/search-empty",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("keyword").description("검색 키워드")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("빈 검색 결과"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }
}
