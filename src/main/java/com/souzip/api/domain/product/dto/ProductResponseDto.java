package com.souzip.api.domain.product.dto;

import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.product.entity.Category;
import com.souzip.api.domain.product.entity.Purpose;
import com.souzip.api.domain.product.entity.Product;

import java.util.List;

public record ProductResponseDto(
        Long id,
        String name,
        Integer price,
        String description,
        Category category,
        Purpose purpose,
        Long cityId,
        List<FileResponse> files
) {

    public static ProductResponseDto from(Product product, List<FileResponse> files) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getCategory(),
                product.getPurpose(),
                product.getCityId(),
                files
        );
    }
}
