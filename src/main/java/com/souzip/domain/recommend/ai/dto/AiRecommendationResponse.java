package com.souzip.domain.recommend.ai.dto;

import java.util.List;

public record AiRecommendationResponse(
        List<RecommendedSouvenir> souvenirs
) {
    public record RecommendedSouvenir(
            Long id,
            String name,
            String category,
            String countryCode,
            String thumbnailUrl,
            long wishlistCount,
            boolean isWishlisted
    ) {
        public static RecommendedSouvenir from(Long id, String name, String category, String countryCode, String thumbnailUrl, long wishlistCount, boolean isWishlisted) {
            return new RecommendedSouvenir(id, name, category, countryCode, thumbnailUrl, wishlistCount, isWishlisted);
        }
    }
}
