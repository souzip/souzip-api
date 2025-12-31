package com.souzip.api.domain.recommend.general.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.category.entity.Category;
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
                                fieldWithPath("data[].thumbnailUrl").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("나라별 추천 기념품 Top10 조회")
    void getCountryTop10() throws Exception {
        // given
        List<GeneralRecommendationDto> responseList = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> new GeneralRecommendationDto(
                        (long) i,
                        "기념품 " + i,
                        Category.SOUVENIR_BASIC,
                        "https://example.com/thumb" + i + ".jpg"
                ))
                .collect(Collectors.toList());

        given(generalRecommendationService.getTop10ByCountry("KR"))
                .willReturn(responseList);

        // when & then
        mockMvc.perform(get("/api/discovery/general/country/KR"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("기념품 1"))
                .andDo(document("recommend/general/country-top10",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("기념품 리스트"),
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING).description("기념품 이름"),
                                fieldWithPath("data[].category").type(JsonFieldType.STRING).description("기념품 카테고리"),
                                fieldWithPath("data[].thumbnailUrl").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("이번 달 나라별 Top3 통계 조회")
    void getTopCountriesThisMonth() throws Exception {
        // given
        GeneralRecommendationStatsDto stat1 = new GeneralRecommendationStatsDto("대한민국", 15L);
        GeneralRecommendationStatsDto stat2 = new GeneralRecommendationStatsDto("일본", 10L);
        GeneralRecommendationStatsDto stat3 = new GeneralRecommendationStatsDto("미국", 7L);
        List<GeneralRecommendationStatsDto> statsList = List.of(stat1, stat2, stat3);

        given(generalRecommendationService.getTop3CountriesByCurrentMonth())
                .willReturn(statsList);

        // when & then
        mockMvc.perform(get("/api/discovery/general/stats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].countryNameKr").value("대한민국"))
                .andExpect(jsonPath("$.data[0].souvenirCount").value(15))
                .andDo(document("recommend/general/stats-top3",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("나라별 기념품 등록 통계"),
                                fieldWithPath("data[].countryNameKr").type(JsonFieldType.STRING).description("나라명 (한글)"),
                                fieldWithPath("data[].souvenirCount").type(JsonFieldType.NUMBER).description("등록된 기념품 개수 (integer)"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }
}
