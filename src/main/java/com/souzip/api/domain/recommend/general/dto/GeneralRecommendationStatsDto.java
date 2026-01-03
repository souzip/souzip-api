package com.souzip.api.domain.recommend.general.dto;

public record GeneralRecommendationStatsDto(
        String countryCode,
        String countryNameKr,
        Long souvenirCount
) {}
