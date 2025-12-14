package com.souzip.api.domain.category.dto;

import com.souzip.api.domain.category.entity.Category;

public record CategoryDto(
    String name,
    String label
) {
    public static CategoryDto from(Category category) {
        return new CategoryDto(
            category.name(),
            category.getLabel()
        );
    }
}
