package com.souzip.api.domain.category.controller;

import com.souzip.api.docs.RestDocsSupport;
import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.category.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

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

class CategoryControllerTest extends RestDocsSupport {

    private final CategoryService categoryService = mock(CategoryService.class);

    @Override
    protected Object initController() {
        return new CategoryController(categoryService);
    }

    @Test
    @DisplayName("카테고리 목록을 조회한다.")
    void getAllCategories_success() throws Exception {
        // given
        List<CategoryDto> categories = List.of(
            new CategoryDto("FOOD_SNACK", "먹거리·간식"),
            new CategoryDto("BEAUTY_HEALTH", "뷰티·헬스"),
            new CategoryDto("FASHION_ACCESSORY", "패션·악세서리"),
            new CategoryDto("CULTURE_TRADITION", "문화·전통"),
            new CategoryDto("SOUVENIR_BASIC", "기념품·기본템")
        );

        given(categoryService.getAllCategories())
            .willReturn(categories);

        // when & then
        mockMvc.perform(get("/api/categories"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(5))
            .andExpect(jsonPath("$.data[0].name").value("FOOD_SNACK"))
            .andExpect(jsonPath("$.data[0].label").value("먹거리·간식"))
            .andExpect(jsonPath("$.data[1].name").value("BEAUTY_HEALTH"))
            .andExpect(jsonPath("$.data[1].label").value("뷰티·헬스"))
            .andDo(document("category/get-all-categories",
                getDocumentRequest(),
                getDocumentResponse(),
                apiResponseFields(
                    fieldWithPath("data").type(JsonFieldType.ARRAY)
                        .description("카테고리 목록"),
                    fieldWithPath("data[].name").type(JsonFieldType.STRING)
                        .description("카테고리 ENUM name (예: FOOD_SNACK)"),
                    fieldWithPath("data[].label").type(JsonFieldType.STRING)
                        .description("카테고리 한글 라벨 (예: 먹거리·간식)"),
                    fieldWithPath("message").type(JsonFieldType.STRING)
                        .description("응답 메시지").optional()
                )
            ));
    }

    @Test
    @DisplayName("카테고리가 비어있어도 빈 배열을 반환한다.")
    void getAllCategories_emptyList() throws Exception {
        // given
        given(categoryService.getAllCategories())
            .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/categories"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }
}
