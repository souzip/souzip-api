package com.souzip.api.domain.search.dto;

import com.souzip.api.domain.search.document.LocationDocument;

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
    Integer priority,
    Float score,
    Map<String, List<String>> highlight
) {
    public static SearchResponse from(LocationDocument document, Float score, Map<String, List<String>> highlight) {
        return new SearchResponse(
            document.getEntityId(),
            document.getType(),
            document.getNameKr(),
            document.getNameEn(),
            document.getNameKr(),
            document.getCountryNameKr(),
            document.getCountryNameEn(),
            document.getCountryNameKr(),
            document.getLatitude(),
            document.getLongitude(),
            document.getPriority(),
            score,
            highlight
        );
    }
}
