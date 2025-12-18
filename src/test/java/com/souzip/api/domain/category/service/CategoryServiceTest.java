package com.souzip.api.domain.category.service;

import com.souzip.api.domain.category.dto.CategoriesResponse;
import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.category.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryServiceTest {

    private final CategoryService categoryService = new CategoryService();

    @Test
    @DisplayName("모든 카테고리 목록을 반환한다.")
    void getAllCategories_returnsAllCategories() {
        // when
        CategoriesResponse response = categoryService.getAllCategories();

        // then
        List<CategoryDto> categories = response.categories();

        assertThat(categories).isNotEmpty();
        assertThat(categories).hasSize(Category.values().length);

        CategoryDto firstCategory = categories.getFirst();
        assertThat(firstCategory.name()).isEqualTo(Category.values()[0].name());
        assertThat(firstCategory.label()).isEqualTo(Category.values()[0].getLabel());
    }

    @Test
    @DisplayName("Category ENUM의 모든 값이 포함된다.")
    void getAllCategories_containsAllEnumValues() {
        // when
        CategoriesResponse response = categoryService.getAllCategories();

        // then
        String[] expectedNames = Arrays.stream(Category.values())
            .map(Enum::name)
            .toArray(String[]::new);

        assertThat(response.categories())
            .extracting(CategoryDto::name)
            .containsExactlyInAnyOrder(expectedNames);
    }

    @Test
    @DisplayName("각 카테고리의 name과 label이 올바르게 매핑된다.")
    void getAllCategories_correctMapping() {
        // when
        CategoriesResponse response = categoryService.getAllCategories();

        // then
        assertThat(response.categories())
            .allSatisfy(dto -> {
                Category category = Category.valueOf(dto.name());
                assertThat(dto.label()).isEqualTo(category.getLabel());
            });
    }

    @Test
    @DisplayName("CategoriesResponse는 불변 객체이다.")
    void categoriesResponse_isImmutable() {
        // when
        CategoriesResponse response = categoryService.getAllCategories();

        // then
        assertThat(response).isNotNull();
        assertThat(response.categories()).isNotNull();

        assertThatThrownBy(() -> response.categories().add(new CategoryDto("TEST", "테스트")))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
