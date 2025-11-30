package com.souzip.api.domain.country.dto;

import com.souzip.api.domain.country.entity.Region;

public record RegionDto(
    String englishName,
    String koreanName
) {
    public static RegionDto from(Region region) {
        return new RegionDto(
            region.getEnglishName(),
            region.getKoreanName()
        );
    }
}
