package com.souzip.api.domain.recommend.ai.dto;

import java.util.List;

public record AiRecommendationResponse(
        List<RecommendedSouvenir> souvenirs
) {
    public record RecommendedSouvenir(
            Long id,
            String name,
            String category,
            String thumbnailUrl
    ) {
        public static RecommendedSouvenir from(Long id, String name, String category, String thumbnailUrl) {
            return new RecommendedSouvenir(id, name, category, thumbnailUrl);
        }
    }
}
