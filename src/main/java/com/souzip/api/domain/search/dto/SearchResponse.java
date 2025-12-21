package com.souzip.api.domain.search.dto;

import com.souzip.api.domain.search.document.LocationDocument;
import java.util.List;
import java.util.Map;

public record SearchResponse(
    String type,
    String name,
    String nameEn,
    String nameKr,
    String countryName,
    String countryNameEn,
    String countryNameKr,
    Float score,
    Map<String, List<String>> highlight
) {
    public static SearchResponse from(LocationDocument document, Float score, Map<String, List<String>> highlight) {
        return new SearchResponse(
            document.getType(),
            document.getNameKr(),
            document.getNameEn(),
            document.getNameKr(),
            document.getCountryNameKr(),
            document.getCountryNameEn(),
            document.getCountryNameKr(),
            score,
            highlight
        );
    }
}
