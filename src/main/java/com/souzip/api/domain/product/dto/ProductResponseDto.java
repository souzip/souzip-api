package com.souzip.api.domain.product.dto;

import com.souzip.api.domain.product.entity.Category;
import com.souzip.api.domain.product.entity.Purpose;
import com.souzip.api.domain.product.entity.Product;

public record ProductResponseDto(
        Long id,
        String name,
        Integer price,
        String description,
        Category category,
        Purpose purpose,
        Long cityId,
        Long userId
) {

    public static ProductResponseDto from(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getCategory(),
                product.getPurpose(),
                product.getCityId(),
                product.getUserId()
        );
    }
}
