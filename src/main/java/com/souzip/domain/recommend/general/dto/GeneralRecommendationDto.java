package com.souzip.domain.recommend.general.dto;

import com.souzip.domain.category.entity.Category;
import com.souzip.domain.souvenir.entity.Souvenir;

public record GeneralRecommendationDto(
        Long id,
        String name,
        Category category,
        String countryCode,
        String thumbnailUrl
) {
    public static GeneralRecommendationDto of(Souvenir souvenir, String thumbnailUrl) {
        return new GeneralRecommendationDto(
                souvenir.getId(),
                souvenir.getName(),
                souvenir.getCategory(),
                souvenir.getCountryCode(),
                thumbnailUrl
        );
    }
}
