package com.souzip.api.domain.recommend.ai.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.recommend.ai.dto.AiRecommendationResponse;
import com.souzip.api.domain.recommend.ai.service.AiRecommendationService;
import com.souzip.api.global.security.annotation.CurrentUserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.souzip.api.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.api.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.api.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AiRecommendationControllerTest extends RestDocsSupport {

    private final AiRecommendationService aiRecommendationService =
            org.mockito.Mockito.mock(AiRecommendationService.class);

    @Override
    protected Object initController() {
        return new AiRecommendationController(aiRecommendationService);
    }

    @BeforeEach
    void setUpWithResolver(RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {

                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(CurrentUserId.class)
                                && parameter.getParameterType().equals(Long.class);
                    }

                    @Override
                    public Object resolveArgument(
                            MethodParameter parameter,
                            ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest,
                            WebDataBinderFactory binderFactory
                    ) {
                        return 1L;
                    }
                })
                .apply(org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
                        .documentationConfiguration(provider))
                .build();
    }

    @Test
    @DisplayName("사용자 선호 카테고리 기반 AI 추천 기념품 조회")
    void getAiCategoryRecommendations() throws Exception {
        // given
        List<AiRecommendationResponse.RecommendedSouvenir> souvenirs = List.of(
                AiRecommendationResponse.RecommendedSouvenir.from(1L, "초코파이", "FOOD_SNACK", "KR", "https://example.com/thumb1.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(2L, "삼다수", "FOOD_SNACK", "KR", "https://example.com/thumb2.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(3L, "설화수 스킨", "BEAUTY_HEALTH", "KR", "https://example.com/thumb3.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(4L, "헤라 립스틱", "BEAUTY_HEALTH", "KR", "https://example.com/thumb4.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(5L, "한복 인형", "CULTURE_TRADITION", "KR", "https://example.com/thumb5.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(6L, "전통 부채", "CULTURE_TRADITION", "KR", "https://example.com/thumb6.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(7L, "여행용 파우치", "TRAVEL_PRACTICAL", "KR", "https://example.com/thumb7.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(8L, "휴대용 보조배터리", "TRAVEL_PRACTICAL", "KR", "https://example.com/thumb8.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(9L, "패션 마스크", "FASHION_ACCESSORY", "KR", "https://example.com/thumb9.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(10L, "손목 시계", "FASHION_ACCESSORY", "KR", "https://example.com/thumb10.jpg")
        );

        given(aiRecommendationService.getCategoryRecommendationsForUser(eq(1L)))
                .willReturn(new AiRecommendationResponse(souvenirs));

        // when & then
        mockMvc.perform(get("/api/discovery/ai/preference-category")
                        .header("Authorization", "Bearer valid_access_token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.souvenirs[0].id").value(1L))
                .andExpect(jsonPath("$.data.souvenirs[0].name").value("초코파이"))
                .andDo(document("recommend/ai/category",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("AI 추천 응답 데이터"),
                                fieldWithPath("data.souvenirs").type(JsonFieldType.ARRAY).description("추천 기념품 리스트"),
                                fieldWithPath("data.souvenirs[].id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                                fieldWithPath("data.souvenirs[].name").type(JsonFieldType.STRING).description("기념품 이름"),
                                fieldWithPath("data.souvenirs[].category").type(JsonFieldType.STRING).description("기념품 카테고리"),
                                fieldWithPath("data.souvenirs[].countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                                fieldWithPath("data.souvenirs[].thumbnailUrl").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("사용자 최근 업로드 기념품 기반 AI 추천 기념품 조회")
    void getAiRecentUploadRecommendations() throws Exception {
        // given
        List<AiRecommendationResponse.RecommendedSouvenir> souvenirs = List.of(
                AiRecommendationResponse.RecommendedSouvenir.from(1L, "모나카", "FOOD_SNACK", "JP", "https://example.com/recent/thumb1.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(2L, "센베이", "FOOD_SNACK", "JP", "https://example.com/recent/thumb2.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(3L, "다이후쿠", "FOOD_SNACK", "JP", "https://example.com/recent/thumb3.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(4L, "가나코", "FOOD_SNACK", "JP", "https://example.com/recent/thumb4.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(5L, "유바", "FOOD_SNACK", "JP", "https://example.com/recent/thumb5.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(6L, "아마자케", "FOOD_SNACK", "JP", "https://example.com/recent/thumb6.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(7L, "호지차", "FOOD_SNACK", "JP", "https://example.com/recent/thumb7.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(8L, "말차초콜릿", "FOOD_SNACK", "JP", "https://example.com/recent/thumb8.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(9L, "시세이도 스킨케어", "BEAUTY_HEALTH", "JP", "https://example.com/recent/thumb9.jpg"),
                AiRecommendationResponse.RecommendedSouvenir.from(10L, "유젠 손수건", "CULTURE_TRADITION", "JP", "https://example.com/recent/thumb10.jpg")
        );

        given(aiRecommendationService.getRecentSouvenirRecommendations(eq(1L)))
                .willReturn(new AiRecommendationResponse(souvenirs));

        // when & then
        mockMvc.perform(get("/api/discovery/ai/preference-upload")
                        .header("Authorization", "Bearer valid_access_token"))
                .andDo(print())
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.data.souvenirs[0].id").value(1L))
                .andExpect(jsonPath("$.data.souvenirs[0].name").value("모나카"))
                .andExpect(jsonPath("$.data.souvenirs[0].category").value("FOOD_SNACK"))
                .andExpect(jsonPath("$.data.souvenirs[0].countryCode").value("JP"))
                .andExpect(jsonPath("$.data.souvenirs[0].thumbnailUrl")
                        .value("https://example.com/recent/thumb1.jpg"))

                .andDo(document("recommend/ai/recent-upload",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("AI 추천 응답 데이터"),
                                fieldWithPath("data.souvenirs").type(JsonFieldType.ARRAY).description("추천 기념품 리스트"),
                                fieldWithPath("data.souvenirs[].id").type(JsonFieldType.NUMBER).description("기념품 ID (integer)"),
                                fieldWithPath("data.souvenirs[].name").type(JsonFieldType.STRING).description("기념품 이름"),
                                fieldWithPath("data.souvenirs[].category").type(JsonFieldType.STRING).description("기념품 카테고리"),
                                fieldWithPath("data.souvenirs[].countryCode").type(JsonFieldType.STRING).description("국가 코드"),
                                fieldWithPath("data.souvenirs[].thumbnailUrl").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @Test
    @DisplayName("사용자 최근 업로드 기념품이 없으면 AI 추천은 빈 배열을 반환한다")
    void getAiRecentUploadRecommendations_empty() throws Exception {
        // given
        given(aiRecommendationService.getRecentSouvenirRecommendations(1L))
                .willReturn(new AiRecommendationResponse(Collections.emptyList()));

        // when & then
        mockMvc.perform(get("/api/discovery/ai/preference-upload")
                        .header("Authorization", "Bearer valid_access_token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.souvenirs").isArray())
                .andExpect(jsonPath("$.data.souvenirs").isEmpty())
                .andDo(document("recommend/ai/recent-upload-empty",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data.souvenirs").type(JsonFieldType.ARRAY)
                                        .description("최근 업로드 이력이 없으면 빈 배열이 반환됩니다"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지").optional()
                        )
                ));
    }
}
