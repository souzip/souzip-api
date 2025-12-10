package com.souzip.api.domain.product.dto;

import com.souzip.api.domain.product.entity.Category;
import com.souzip.api.domain.product.entity.Purpose;

public record ProductUpdateRequestDto(
        String name,
        Integer price,
        String imageUrl,
        String description,
        Category category,
        Purpose purpose,
        String location,
        String address
) {}
