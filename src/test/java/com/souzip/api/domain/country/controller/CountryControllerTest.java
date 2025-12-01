package com.souzip.api.domain.country.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.country.dto.CountryResponseDto;
import com.souzip.api.domain.country.dto.CountryListResponse;
import com.souzip.api.domain.country.dto.RegionDto;
import com.souzip.api.domain.country.service.CountryService;
import com.souzip.api.domain.currency.CurrencyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.math.BigDecimal;
import java.util.List;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CountryControllerTest extends RestDocsSupport {

    private final CountryService countryService = mock(CountryService.class);

    @Override
    protected Object initController() {
        return new CountryController(countryService);
    }

    private CountryResponseDto createCountryResponse(
        String nameEn,
        String nameKr,
        String code,
        String regionEn,
        String regionKr,
        String capital,
        String imageUrl,
        String latitude,
        String longitude,
        String currencyCode,
        String currencySymbol
    ) {
        RegionDto region = new RegionDto(regionEn, regionKr);
        CurrencyDto currency = new CurrencyDto(currencyCode, currencySymbol);

        return new CountryResponseDto(
            nameEn,
            nameKr,
            code,
            region,
            capital,
            imageUrl,
            new BigDecimal(latitude),
            new BigDecimal(longitude),
            currency
        );
    }

    @Test
    @DisplayName("전체 국가 목록을 조회한다")
    void getAllCountries() throws Exception {
        // given
        CountryResponseDto korea = createCountryResponse(
            "South Korea", "대한민국", "KR", "Asia", "아시아",
            "Seoul", "https://flagcdn.com/w320/kr.png",
            "37.0", "127.0", "KRW", "₩"
        );
        CountryResponseDto france = createCountryResponse(
            "France", "프랑스", "FR", "Europe", "유럽",
            "Paris", "https://flagcdn.com/w320/fr.png",
            "46.0", "2.0", "EUR", "€"
        );

        CountryListResponse response = CountryListResponse.from(List.of(korea, france));
        given(countryService.getAllCountries()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/countries"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.countries").isArray())
            .andExpect(jsonPath("$.data.countries[0].code").value("KR"))
            .andExpect(jsonPath("$.data.countries[1].code").value("FR"))
            .andDo(document("country/get-all",
                getDocumentRequest(),
                getDocumentResponse(),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                    fieldWithPath("data.countries[]").type(JsonFieldType.ARRAY).description("국가 목록"),
                    fieldWithPath("data.countries[].nameEn").type(JsonFieldType.STRING).description("국가명 (영문)"),
                    fieldWithPath("data.countries[].nameKr").type(JsonFieldType.STRING).description("국가명 (한글)"),
                    fieldWithPath("data.countries[].code").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("data.countries[].region").type(JsonFieldType.OBJECT).description("지역 정보"),
                    fieldWithPath("data.countries[].region.englishName").type(JsonFieldType.STRING).description("지역 영문명"),
                    fieldWithPath("data.countries[].region.koreanName").type(JsonFieldType.STRING).description("지역 한글명"),
                    fieldWithPath("data.countries[].capital").type(JsonFieldType.STRING).description("수도"),
                    fieldWithPath("data.countries[].imageUrl").type(JsonFieldType.STRING).description("국기 이미지 URL"),
                    fieldWithPath("data.countries[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                    fieldWithPath("data.countries[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                    fieldWithPath("data.countries[].currency").type(JsonFieldType.OBJECT).description("통화 정보"),
                    fieldWithPath("data.countries[].currency.code").type(JsonFieldType.STRING).description("통화 코드"),
                    fieldWithPath("data.countries[].currency.symbol").type(JsonFieldType.STRING).description("통화 기호"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("국가 코드로 특정 국가를 조회한다")
    void getCountryByCode() throws Exception {
        // given
        CountryResponseDto korea = createCountryResponse(
            "South Korea", "대한민국", "KR", "Asia", "아시아",
            "Seoul", "https://flagcdn.com/w320/kr.png",
            "37.0", "127.0", "KRW", "₩"
        );

        given(countryService.getCountryByCode("KR")).willReturn(korea);

        // when & then
        mockMvc.perform(get("/api/countries/{code}", "KR"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.code").value("KR"))
            .andExpect(jsonPath("$.data.nameEn").value("South Korea"))
            .andExpect(jsonPath("$.data.nameKr").value("대한민국"))
            .andDo(document("country/get-by-code",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("code").description("국가 코드")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("국가 정보"),
                    fieldWithPath("data.nameEn").type(JsonFieldType.STRING).description("국가명 (영문)"),
                    fieldWithPath("data.nameKr").type(JsonFieldType.STRING).description("국가명 (한글)"),
                    fieldWithPath("data.code").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("data.region").type(JsonFieldType.OBJECT).description("지역 정보"),
                    fieldWithPath("data.region.englishName").type(JsonFieldType.STRING).description("지역 영문명"),
                    fieldWithPath("data.region.koreanName").type(JsonFieldType.STRING).description("지역 한글명"),
                    fieldWithPath("data.capital").type(JsonFieldType.STRING).description("수도"),
                    fieldWithPath("data.imageUrl").type(JsonFieldType.STRING).description("국기 이미지 URL"),
                    fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도"),
                    fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도"),
                    fieldWithPath("data.currency").type(JsonFieldType.OBJECT).description("통화 정보"),
                    fieldWithPath("data.currency.code").type(JsonFieldType.STRING).description("통화 코드"),
                    fieldWithPath("data.currency.symbol").type(JsonFieldType.STRING).description("통화 기호"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("지역별 국가 목록을 조회한다")
    void getCountriesByRegion() throws Exception {
        // given
        CountryResponseDto korea = createCountryResponse(
            "South Korea", "대한민국", "KR", "Asia", "아시아",
            "Seoul", "https://flagcdn.com/w320/kr.png",
            "37.0", "127.0", "KRW", "₩"
        );
        CountryResponseDto japan = createCountryResponse(
            "Japan", "일본", "JP", "Asia", "아시아",
            "Tokyo", "https://flagcdn.com/w320/jp.png",
            "36.0", "138.0", "JPY", "¥"
        );

        CountryListResponse response = CountryListResponse.from(List.of(korea, japan));
        given(countryService.getCountriesByRegion("Asia")).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/countries/region/{englishName}", "Asia"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.countries").isArray())
            .andExpect(jsonPath("$.data.countries").isNotEmpty())
            .andDo(document("country/get-by-region",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("englishName").description("지역 영문명")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                    fieldWithPath("data.countries[]").type(JsonFieldType.ARRAY).description("국가 목록"),
                    fieldWithPath("data.countries[].nameEn").type(JsonFieldType.STRING).description("국가명 (영문)"),
                    fieldWithPath("data.countries[].nameKr").type(JsonFieldType.STRING).description("국가명 (한글)"),
                    fieldWithPath("data.countries[].code").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("data.countries[].region").type(JsonFieldType.OBJECT).description("지역 정보"),
                    fieldWithPath("data.countries[].region.englishName").type(JsonFieldType.STRING).description("지역 영문명"),
                    fieldWithPath("data.countries[].region.koreanName").type(JsonFieldType.STRING).description("지역 한글명"),
                    fieldWithPath("data.countries[].capital").type(JsonFieldType.STRING).description("수도"),
                    fieldWithPath("data.countries[].imageUrl").type(JsonFieldType.STRING).description("국기 이미지 URL"),
                    fieldWithPath("data.countries[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                    fieldWithPath("data.countries[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                    fieldWithPath("data.countries[].currency").type(JsonFieldType.OBJECT).description("통화 정보"),
                    fieldWithPath("data.countries[].currency.code").type(JsonFieldType.STRING).description("통화 코드"),
                    fieldWithPath("data.countries[].currency.symbol").type(JsonFieldType.STRING).description("통화 기호"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("국가명으로 검색한다")
    void searchCountries() throws Exception {
        // given
        CountryResponseDto korea = createCountryResponse(
            "South Korea", "대한민국", "KR", "Asia", "아시아",
            "Seoul", "https://flagcdn.com/w320/kr.png",
            "37.0", "127.0", "KRW", "₩"
        );

        CountryListResponse response = CountryListResponse.from(List.of(korea));
        given(countryService.searchCountriesByName("Korea")).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/countries/search")
                .param("name", "Korea"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.countries").isArray())
            .andExpect(jsonPath("$.data.countries[0].nameEn").value("South Korea"))
            .andDo(document("country/search",
                getDocumentRequest(),
                getDocumentResponse(),
                queryParameters(
                    parameterWithName("name").description("검색할 국가명 (부분 일치, 영문/한글)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                    fieldWithPath("data.countries[]").type(JsonFieldType.ARRAY).description("검색 결과"),
                    fieldWithPath("data.countries[].nameEn").type(JsonFieldType.STRING).description("국가명 (영문)"),
                    fieldWithPath("data.countries[].nameKr").type(JsonFieldType.STRING).description("국가명 (한글)"),
                    fieldWithPath("data.countries[].code").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("data.countries[].region").type(JsonFieldType.OBJECT).description("지역 정보"),
                    fieldWithPath("data.countries[].region.englishName").type(JsonFieldType.STRING).description("지역 영문명"),
                    fieldWithPath("data.countries[].region.koreanName").type(JsonFieldType.STRING).description("지역 한글명"),
                    fieldWithPath("data.countries[].capital").type(JsonFieldType.STRING).description("수도"),
                    fieldWithPath("data.countries[].imageUrl").type(JsonFieldType.STRING).description("국기 이미지 URL"),
                    fieldWithPath("data.countries[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                    fieldWithPath("data.countries[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                    fieldWithPath("data.countries[].currency").type(JsonFieldType.OBJECT).description("통화 정보"),
                    fieldWithPath("data.countries[].currency.code").type(JsonFieldType.STRING).description("통화 코드"),
                    fieldWithPath("data.countries[].currency.symbol").type(JsonFieldType.STRING).description("통화 기호"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("지역별 국가 수를 조회한다")
    void getCountryCountByRegion() throws Exception {
        // given
        given(countryService.getCountryCountByRegion("Asia")).willReturn(48L);

        // when & then
        mockMvc.perform(get("/api/countries/region/{englishName}/count", "Asia"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(48))
            .andDo(document("country/get-count-by-region",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("englishName").description("지역 영문명")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.NUMBER).description("해당 지역의 국가 수"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }
}
