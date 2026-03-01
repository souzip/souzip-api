package com.souzip.domain.country.dto;

import com.souzip.domain.country.entity.Region;

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
