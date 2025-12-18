package com.souzip.api.domain.category.dto;

import java.util.List;

public record CategoriesResponse(
    List<CategoryDto> categories
) {
    public static CategoriesResponse of(List<CategoryDto> categories) {
        return new CategoriesResponse(categories);
    }
}
