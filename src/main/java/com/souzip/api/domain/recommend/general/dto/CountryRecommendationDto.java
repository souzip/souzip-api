package com.souzip.api.domain.recommend.general.dto;

import java.util.List;

public record CountryRecommendationDto(
        String countryCode,
        String countryNameKr,
        Long souvenirCount,
        List<GeneralRecommendationDto> souvenirs
) {}
