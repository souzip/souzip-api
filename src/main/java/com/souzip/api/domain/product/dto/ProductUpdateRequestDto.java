package com.souzip.api.domain.product.dto;

import com.souzip.api.domain.product.entity.Category;
import com.souzip.api.domain.product.entity.Purpose;

public record ProductUpdateRequestDto(
        String name,
        Integer price,
        String description,
        Category category,
        Purpose purpose,
        Long cityId
) {}
