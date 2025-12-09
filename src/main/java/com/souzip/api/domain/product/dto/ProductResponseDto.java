package com.souzip.api.domain.product.dto;

import com.souzip.api.domain.product.entity.Category;
import com.souzip.api.domain.product.entity.Purpose;

public record ProductResponseDto(
        Long id,
        String name,
        Integer price,
        String imageUrl,
        String description,
        Category category,
        Purpose purpose,
        String location,
        String address
) {

    public static ProductResponseDto fromEntity(com.souzip.api.domain.product.entity.Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getDescription(),
                product.getCategory(),
                product.getPurpose(),
                product.getLocation(),
                product.getAddress()
        );
    }
}
