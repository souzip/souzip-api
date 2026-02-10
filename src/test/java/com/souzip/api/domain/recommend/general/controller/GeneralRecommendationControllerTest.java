package com.souzip.api.domain.recommend.general.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.recommend.general.dto.CountryRecommendationDto;
import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationDto;
import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationStatsDto;
import com.souzip.api.domain.recommend.general.service.GeneralRecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.restdocs.payload.JsonFieldType;

class GeneralRecommendationControllerTest extends RestDocsSupport {

    private final GeneralRecommendationService generalRecommendationService = mock(GeneralRecommendationService.class);

    @Override
    protected Object initController() {
        return new GeneralRecommendationController(generalRecommendationService);
    }

    @Test
    @DisplayName("카테고리별 추천 기념품 Top10 조회")
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
        mockMvc.perform(get("/api/discovery/general/category/FOOD_SNACK"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andDo(document("recommend/general/category-top10",
                        getDocumentRequest(),
                        getDocumentResponse(),
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

    @Test
    @DisplayName("기념품 최다 등록 국가 Top(최대10) + 국가별 기념품 Top10 조회")
    void getTopCountriesWithTop10Souvenirs() throws Exception {
        // given
        List<CountryRecommendationDto> responseList = List.of(
                new CountryRecommendationDto(
                        "KR",
                        "대한민국",
                        123L,
                        IntStream.rangeClosed(1, 10)
                                .mapToObj(i -> new GeneralRecommendationDto(
                                        (long) i,
                                        "KR 기념품 " + i,
                                        Category.SOUVENIR_BASIC,
                                        "KR",
                                        "https://example.com/kr/thumb" + i + ".jpg"
                                ))
                                .toList()
                ),
                new CountryRecommendationDto(
                        "JP",
                        "일본",
                        45L,
                        IntStream.rangeClosed(1, 10)
                                .mapToObj(i -> new GeneralRecommendationDto(
                                        (long) (100 + i),
                                        "JP 기념품 " + i,
                                        Category.SOUVENIR_BASIC,
                                        "JP",
                                        "https://example.com/jp/thumb" + i + ".jpg"
                                ))
                                .toList()
                )
        );

        given(generalRecommendationService.getTopCountriesWithTop10Souvenirs())
                .willReturn(responseList);

        // when & then
        mockMvc.perform(get("/api/discovery/general/country"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].countryCode").value("KR"))
                .andExpect(jsonPath("$.data[0].souvenirs").isArray())
                .andExpect(jsonPath("$.data[0].souvenirs[0].id").value(1))
                .andExpect(jsonPath("$.data[1].countryCode").value("JP"))
                .andDo(document("recommend/general/country-top10",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("국가별 추천 결과 리스트 (최대 10개 국가)"),
                                fieldWithPath("data[].countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                                fieldWithPath("data[].countryNameKr").type(JsonFieldType.STRING).description("국가명(한글)"),
                                fieldWithPath("data[].souvenirCount").type(JsonFieldType.NUMBER).description("해당 국가 기념품 등록 수 (integer)"),
                                fieldWithPath("data[].souvenirs").type(JsonFieldType.ARRAY).description("해당 국가 추천 기념품 Top10 리스트"),
                                fieldWithPath("data[].souvenirs[].id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                                fieldWithPath("data[].souvenirs[].name").type(JsonFieldType.STRING).description("기념품 이름"),
                                fieldWithPath("data[].souvenirs[].category").type(JsonFieldType.STRING).description("기념품 카테고리"),
                                fieldWithPath("data[].souvenirs[].countryCode").type(JsonFieldType.STRING).description("기념품 국가 코드"),
                                fieldWithPath("data[].souvenirs[].thumbnailUrl").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("기념품 최다등록 국가 Top3 통계 조회")
    void getTopCountriesThisMonth() throws Exception {
        // given
        GeneralRecommendationStatsDto stat1 = new GeneralRecommendationStatsDto("JP", "일본", 20L);
        GeneralRecommendationStatsDto stat2 = new GeneralRecommendationStatsDto("US", "미국", 15L);
        GeneralRecommendationStatsDto stat3 = new GeneralRecommendationStatsDto("TW", "대만", 10L);

        List<GeneralRecommendationStatsDto> statsList =
                List.of(stat1, stat2, stat3);

        given(generalRecommendationService.getTop3CountriesBySouvenirCount())
                .willReturn(statsList);

        // when & then
        mockMvc.perform(get("/api/discovery/general/stats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].countryNameKr").value("일본"))
                .andExpect(jsonPath("$.data[0].countryCode").value("JP"))
                .andExpect(jsonPath("$.data[1].countryCode").value("US"))
                .andExpect(jsonPath("$.data[2].countryCode").value("TW"))
                .andDo(document("recommend/general/stats-top3",
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
}
