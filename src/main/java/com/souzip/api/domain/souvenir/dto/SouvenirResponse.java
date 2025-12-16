package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.souvenir.entity.Purpose;
import com.souzip.api.domain.souvenir.entity.Souvenir;

import java.util.List;

public record SouvenirResponse(
        Long id,
        String name,
        Integer price,
        String description,
        Category category,
        Purpose purpose,
        Long cityId,
        List<FileResponse> files
) {

    public static SouvenirResponse from(Souvenir souvenir, List<FileResponse> files) {
        return new SouvenirResponse(
                souvenir.getId(),
                souvenir.getName(),
                souvenir.getPrice(),
                souvenir.getDescription(),
                souvenir.getCategory(),
                souvenir.getPurpose(),
                souvenir.getCityId(),
                files
        );
    }
}
