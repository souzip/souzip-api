package com.souzip.domain.recommend.general.controller;

import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.category.entity.Category;
import com.souzip.domain.recommend.general.dto.CountryRecommendationDto;
import com.souzip.domain.recommend.general.dto.GeneralRecommendationDto;
import com.souzip.domain.recommend.general.dto.GeneralRecommendationStatsDto;
import com.souzip.domain.recommend.general.service.GeneralRecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GeneralRecommendationControllerTest extends RestDocsSupport {

    private final GeneralRecommendationService generalRecommendationService = mock(GeneralRecommendationService.class);

    @Override
    protected Object initController() {
        return new GeneralRecommendationController(generalRecommendationService);
    }

    @DisplayName("카테고리별 추천 기념품 Top10 조회")
    @Test
    void getCategoryTop10() throws Exception {
        // given
        List<GeneralRecommendationDto> responseList = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> new GeneralRecommendationDto(
                (long) i,
                "기념품 " + i,
                Category.SOUVENIR_BASIC,
                "JP",
                "https://example.com/thumb" + i + ".jpg"
            ))
            .collect(Collectors.toList());

        given(generalRecommendationService.getTop10ByCategory("FOOD_SNACK"))
            .willReturn(responseList);

        // when & then
        mockMvc.perform(get("/api/discovery/general/category/{categoryName}", "FOOD_SNACK"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value(1))
            .andDo(document("recommend/category-top10",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("categoryName").description("조회할 카테고리 이름 (예: FOOD_SNACK)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.ARRAY).description("기념품 리스트"),
                    fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                    fieldWithPath("data[].name").type(JsonFieldType.STRING).description("기념품 이름"),
                    fieldWithPath("data[].category").type(JsonFieldType.STRING).description("기념품 카테고리"),
                    fieldWithPath("data[].countryCode").type(JsonFieldType.STRING).description("기념품 국가 코드"),
                    fieldWithPath("data[].thumbnailUrl").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }

    @Deprecated
    @DisplayName("나라별 추천 기념품 Top10 조회")
    @Test
    void getCountryTop10() throws Exception {
        // given
        List<GeneralRecommendationDto> responseList = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> new GeneralRecommendationDto(
                (long) i,
                "기념품 " + i,
                Category.SOUVENIR_BASIC,
                "KR",
                "https://example.com/thumb" + i + ".jpg"
            ))
            .collect(Collectors.toList());

        given(generalRecommendationService.getTop10ByCountry("KR"))
            .willReturn(responseList);

        // when & then
        mockMvc.perform(get("/api/discovery/general/country/{countryCode}", "KR"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value(1))
            .andExpect(jsonPath("$.data[0].name").value("기념품 1"))
            .andDo(document("recommend/country-top10",
                getDocumentRequest(),
                getDocumentResponse(),
                pathParameters(
                    parameterWithName("countryCode").description("조회할 국가 코드 (예: KR, JP)")
                ),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.ARRAY).description("기념품 리스트"),
                    fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                    fieldWithPath("data[].name").type(JsonFieldType.STRING).description("기념품 이름"),
                    fieldWithPath("data[].category").type(JsonFieldType.STRING).description("기념품 카테고리"),
                    fieldWithPath("data[].countryCode").type(JsonFieldType.STRING).description("기념품 국가 코드"),
                    fieldWithPath("data[].thumbnailUrl").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }

    @Deprecated
    @DisplayName("기념품 최다등록 국가 Top10 조회(통계만)")
    @Test
    void getTopCountriesTop10() throws Exception {
        // given
        List<GeneralRecommendationStatsDto> statsList = List.of(
            new GeneralRecommendationStatsDto("JP", "일본", 20L),
            new GeneralRecommendationStatsDto("US", "미국", 15L),
            new GeneralRecommendationStatsDto("TW", "대만", 10L),
            new GeneralRecommendationStatsDto("HK", "홍콩", 8L),
            new GeneralRecommendationStatsDto("VN", "베트남", 4L),
            new GeneralRecommendationStatsDto("KR", "한국", 4L),
            new GeneralRecommendationStatsDto("DE", "독일", 4L),
            new GeneralRecommendationStatsDto("FR", "프랑스", 4L),
            new GeneralRecommendationStatsDto("CN", "중국", 3L),
            new GeneralRecommendationStatsDto("PH", "필리핀", 3L)
        );

        given(generalRecommendationService.getTop10CountriesBySouvenirCount())
            .willReturn(statsList);

        // when & then
        mockMvc.perform(get("/api/discovery/general/countries/top10"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(10))
            .andDo(document("recommend/stats-top10",
                getDocumentRequest(),
                getDocumentResponse(),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.ARRAY).description("나라별 기념품 등록 통계(누적 Top10)"),
                    fieldWithPath("data[].countryNameKr").type(JsonFieldType.STRING).description("나라명 (한글)"),
                    fieldWithPath("data[].countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("data[].souvenirCount").type(JsonFieldType.NUMBER).description("등록된 기념품 개수 (integer)"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }

    @Deprecated
    @DisplayName("기념품 최다등록 국가 Top3 조회(통계만)")
    @Test
    void getTopCountriesTop3() throws Exception {
        // given
        List<GeneralRecommendationStatsDto> statsList = List.of(
            new GeneralRecommendationStatsDto("JP", "일본", 20L),
            new GeneralRecommendationStatsDto("US", "미국", 15L),
            new GeneralRecommendationStatsDto("TW", "대만", 10L)
        );

        given(generalRecommendationService.getTop3CountriesBySouvenirCount())
            .willReturn(statsList);

        // when & then
        mockMvc.perform(get("/api/discovery/general/stats"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(3))
            .andDo(document("recommend/stats-top3",
                getDocumentRequest(),
                getDocumentResponse(),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.ARRAY).description("나라별 기념품 등록 통계(누적 Top3)"),
                    fieldWithPath("data[].countryNameKr").type(JsonFieldType.STRING).description("나라명 (한글)"),
                    fieldWithPath("data[].countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("data[].souvenirCount").type(JsonFieldType.NUMBER).description("등록된 기념품 개수 (integer)"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }

    @DisplayName("기념품 최다등록 Top10 국가 + 국가별 기념품 Top10 조회")
    @Test
    void getTopCountriesWithTop10Souvenirs() throws Exception {
        // given
        List<GeneralRecommendationDto> jpSouvenirs = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> new GeneralRecommendationDto(
                200L + i,
                "일본 기념품 " + i,
                Category.SOUVENIR_BASIC,
                "JP",
                "https://example.com/jp/thumb" + i + ".jpg"
            ))
            .collect(Collectors.toList());

        List<GeneralRecommendationDto> usSouvenirs = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> new GeneralRecommendationDto(
                300L + i,
                "미국 기념품 " + i,
                Category.SOUVENIR_BASIC,
                "US",
                "https://example.com/us/thumb" + i + ".jpg"
            ))
            .collect(Collectors.toList());

        List<CountryRecommendationDto> response = List.of(
            new CountryRecommendationDto("JP", "일본", 20L, jpSouvenirs),
            new CountryRecommendationDto("US", "미국", 15L, usSouvenirs)
        );

        given(generalRecommendationService.getTopCountriesWithTop10Souvenirs())
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/countries/souvenirs"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andDo(document("recommend/countries-top10-souvenirs-top10",
                getDocumentRequest(),
                getDocumentResponse(),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.ARRAY).description("기념품 최다등록 국가 Top10 및 국가별 기념품 Top10"),
                    fieldWithPath("data[].countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                    fieldWithPath("data[].countryNameKr").type(JsonFieldType.STRING).description("나라명 (한글)"),
                    fieldWithPath("data[].souvenirCount").type(JsonFieldType.NUMBER).description("등록된 기념품 개수 (integer)"),
                    fieldWithPath("data[].souvenirs").type(JsonFieldType.ARRAY).description("해당 국가의 추천 기념품 리스트(최대 10개)"),
                    fieldWithPath("data[].souvenirs[].id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                    fieldWithPath("data[].souvenirs[].name").type(JsonFieldType.STRING).description("기념품 이름"),
                    fieldWithPath("data[].souvenirs[].category").type(JsonFieldType.STRING).description("기념품 카테고리"),
                    fieldWithPath("data[].souvenirs[].countryCode").type(JsonFieldType.STRING).description("기념품 국가 코드"),
                    fieldWithPath("data[].souvenirs[].thumbnailUrl").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                )
            ));
    }
}
