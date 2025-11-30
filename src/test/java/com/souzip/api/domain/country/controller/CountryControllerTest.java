package com.souzip.api.domain.country.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.country.dto.CountryResponseDto;
import com.souzip.api.domain.country.dto.RegionDto;
import com.souzip.api.domain.country.service.CountryService;
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

    @Test
    @DisplayName("전체 국가 목록을 조회한다.")
    void getAllCountries() throws Exception {
        // given
        RegionDto asiaRegion = new RegionDto("Asia", "아시아");
        RegionDto europeRegion = new RegionDto("Europe", "유럽");

        CountryResponseDto korea = new CountryResponseDto(
            "South Korea",
            "KR",
            asiaRegion,
            "Seoul",
            "https://flagcdn.com/w320/kr.png",
            new BigDecimal("37.0"),
            new BigDecimal("127.0")
        );

        CountryResponseDto france = new CountryResponseDto(
            "France",
            "FR",
            europeRegion,
            "Paris",
            "https://flagcdn.com/w320/fr.png",
            new BigDecimal("46.0"),
            new BigDecimal("2.0")
        );

        given(countryService.getAllCountries()).willReturn(List.of(korea, france));

        // when & then
        mockMvc.perform(get("/api/countries"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].code").value("KR"))
            .andExpect(jsonPath("$.data[1].code").value("FR"))
            .andDo(document("country/get-all",
                getDocumentRequest(),
                getDocumentResponse(),
                apiResponseFields(
                    fieldWithPath("data[]").type(JsonFieldType.ARRAY).description("국가 목록"),
                    fieldWithPath("data[].name").type(JsonFieldType.STRING).description("국가명"),
                    fieldWithPath("data[].code").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("data[].region").type(JsonFieldType.OBJECT).description("지역 정보"),
                    fieldWithPath("data[].region.englishName").type(JsonFieldType.STRING).description("지역 영문명"),
                    fieldWithPath("data[].region.koreanName").type(JsonFieldType.STRING).description("지역 한글명"),
                    fieldWithPath("data[].capital").type(JsonFieldType.STRING).description("수도"),
                    fieldWithPath("data[].flagUrl").type(JsonFieldType.STRING).description("국기 이미지 URL"),
                    fieldWithPath("data[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                    fieldWithPath("data[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("국가 코드로 특정 국가를 조회한다.")
    void getCountryByCode() throws Exception {
        // given
        RegionDto asiaRegion = new RegionDto("Asia", "아시아");
        CountryResponseDto korea = new CountryResponseDto(
            "South Korea",
            "KR",
            asiaRegion,
            "Seoul",
            "https://flagcdn.com/w320/kr.png",
            new BigDecimal("37.0"),
            new BigDecimal("127.0")
        );

        given(countryService.getCountryByCode("KR")).willReturn(korea);

        // when & then
        mockMvc.perform(get("/api/countries/{code}", "KR"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.code").value("KR"))
            .andExpect(jsonPath("$.data.name").value("South Korea"))
            .andDo(document("country/get-by-code",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("code").description("국가 코드")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("국가 정보"),
                    fieldWithPath("data.name").type(JsonFieldType.STRING).description("국가명"),
                    fieldWithPath("data.code").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("data.region").type(JsonFieldType.OBJECT).description("지역 정보"),
                    fieldWithPath("data.region.englishName").type(JsonFieldType.STRING).description("지역 영문명"),
                    fieldWithPath("data.region.koreanName").type(JsonFieldType.STRING).description("지역 한글명"),
                    fieldWithPath("data.capital").type(JsonFieldType.STRING).description("수도"),
                    fieldWithPath("data.flagUrl").type(JsonFieldType.STRING).description("국기 이미지 URL"),
                    fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도"),
                    fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("지역별 국가 목록을 조회한다.")
    void getCountriesByRegion() throws Exception {
        // given
        RegionDto asiaRegion = new RegionDto("Asia", "아시아");
        CountryResponseDto korea = new CountryResponseDto(
            "South Korea",
            "KR",
            asiaRegion,
            "Seoul",
            "https://flagcdn.com/w320/kr.png",
            new BigDecimal("37.0"),
            new BigDecimal("127.0")
        );

        CountryResponseDto japan = new CountryResponseDto(
            "Japan",
            "JP",
            asiaRegion,
            "Tokyo",
            "https://flagcdn.com/w320/jp.png",
            new BigDecimal("36.0"),
            new BigDecimal("138.0")
        );

        given(countryService.getCountriesByRegion("Asia")).willReturn(List.of(korea, japan));

        // when & then
        mockMvc.perform(get("/api/countries/region/{englishName}", "Asia"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].region.englishName").value("Asia"))
            .andDo(document("country/get-by-region",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("englishName").description("지역 영문명")
                ),
                apiResponseFields(
                    fieldWithPath("data[]").type(JsonFieldType.ARRAY).description("국가 목록"),
                    fieldWithPath("data[].name").type(JsonFieldType.STRING).description("국가명"),
                    fieldWithPath("data[].code").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("data[].region").type(JsonFieldType.OBJECT).description("지역 정보"),
                    fieldWithPath("data[].region.englishName").type(JsonFieldType.STRING).description("지역 영문명"),
                    fieldWithPath("data[].region.koreanName").type(JsonFieldType.STRING).description("지역 한글명"),
                    fieldWithPath("data[].capital").type(JsonFieldType.STRING).description("수도"),
                    fieldWithPath("data[].flagUrl").type(JsonFieldType.STRING).description("국기 이미지 URL"),
                    fieldWithPath("data[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                    fieldWithPath("data[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("국가명으로 검색한다.")
    void searchCountries() throws Exception {
        // given
        RegionDto asiaRegion = new RegionDto("Asia", "아시아");
        CountryResponseDto korea = new CountryResponseDto(
            "South Korea",
            "KR",
            asiaRegion,
            "Seoul",
            "https://flagcdn.com/w320/kr.png",
            new BigDecimal("37.0"),
            new BigDecimal("127.0")
        );

        given(countryService.searchCountriesByName("Korea")).willReturn(List.of(korea));

        // when & then
        mockMvc.perform(get("/api/countries/search")
                .param("name", "Korea"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].name").value("South Korea"))
            .andDo(document("country/search",
                getDocumentRequest(),
                getDocumentResponse(),
                queryParameters(
                    parameterWithName("name").description("검색할 국가명 (부분 일치)")
                ),
                apiResponseFields(
                    fieldWithPath("data[]").type(JsonFieldType.ARRAY).description("검색 결과"),
                    fieldWithPath("data[].name").type(JsonFieldType.STRING).description("국가명"),
                    fieldWithPath("data[].code").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("data[].region").type(JsonFieldType.OBJECT).description("지역 정보"),
                    fieldWithPath("data[].region.englishName").type(JsonFieldType.STRING).description("지역 영문명"),
                    fieldWithPath("data[].region.koreanName").type(JsonFieldType.STRING).description("지역 한글명"),
                    fieldWithPath("data[].capital").type(JsonFieldType.STRING).description("수도"),
                    fieldWithPath("data[].flagUrl").type(JsonFieldType.STRING).description("국기 이미지 URL"),
                    fieldWithPath("data[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                    fieldWithPath("data[].longitude").type(JsonFieldType.NUMBER).description("경도"),
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
