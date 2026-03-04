package com.souzip.domain.search.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record SearchResponse(
        Long id,
        String type,
        String name,
        String nameEn,
        String nameKr,
        String countryName,
        String countryNameEn,
        String countryNameKr,
        BigDecimal latitude,
        BigDecimal longitude,
        Float score,
        Map<String, List<String>> highlight
) {
    public static SearchResponse of(
            Long id,
            String type,
            String nameKr,
            String nameEn,
            String countryNameKr,
            String countryNameEn,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        return new SearchResponse(
                id,
                type,
                nameKr,
                nameEn,
                nameKr,
                countryNameKr,
                countryNameEn,
                countryNameKr,
                latitude,
                longitude,
                0.0f,
                Map.of()
        );
    }
}
