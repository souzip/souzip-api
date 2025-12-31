package com.souzip.api.domain.recommend.general.dto;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.souvenir.entity.Souvenir;

public record GeneralRecommendationDto(
        Long id,
        String name,
        Category category,
        String thumbnailUrl
) {
    public static GeneralRecommendationDto of(Souvenir souvenir, String thumbnailUrl) {
        return new GeneralRecommendationDto(
                souvenir.getId(),
                souvenir.getName(),
                souvenir.getCategory(),
                thumbnailUrl
        );
    }
}
