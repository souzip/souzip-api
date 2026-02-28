package com.souzip.api.domain.search.dto;

import java.math.BigDecimal;

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
        Double score,
        String highlight

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
                null,
                null
        );
    }
}
