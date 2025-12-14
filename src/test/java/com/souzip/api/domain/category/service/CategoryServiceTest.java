package com.souzip.api.domain.category.service;

import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.category.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryServiceTest {

    private final CategoryService categoryService = new CategoryService();

    @Test
    @DisplayName("모든 카테고리 목록을 반환한다.")
    void getAllCategories_returnsAllCategories() {
        // when
        List<CategoryDto> categories = categoryService.getAllCategories();

        // then
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
        List<CategoryDto> categories = categoryService.getAllCategories();

        // then
        List<String> categoryNames = categories.stream()
            .map(CategoryDto::name)
            .toList();

        for (Category category : Category.values()) {
            assertThat(categoryNames).contains(category.name());
        }
    }

    @Test
    @DisplayName("각 카테고리의 name과 label이 올바르게 매핑된다.")
    void getAllCategories_correctMapping() {
        // when
        List<CategoryDto> categories = categoryService.getAllCategories();

        // then
        for (CategoryDto dto : categories) {
            Category category = Category.valueOf(dto.name());
            assertThat(dto.label()).isEqualTo(category.getLabel());
        }
    }
}
