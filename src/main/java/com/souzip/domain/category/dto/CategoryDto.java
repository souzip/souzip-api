package com.souzip.domain.category.dto;

import com.souzip.domain.category.entity.Category;

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
