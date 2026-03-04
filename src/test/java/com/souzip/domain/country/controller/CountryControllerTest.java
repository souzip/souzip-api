package com.souzip.domain.country.controller;

import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.country.dto.CountryResponseDto;
import com.souzip.domain.country.dto.CountryListResponse;
import com.souzip.domain.country.dto.RegionDto;
import com.souzip.domain.country.service.CountryService;
import com.souzip.domain.currency.dto.CurrencyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.math.BigDecimal;
import java.util.List;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
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
}
