package com.souzip.api.domain.country.dto;

import com.souzip.api.domain.country.entity.Country;
import java.math.BigDecimal;

public record CountryResponseDto(
    String name,
    String code,
    RegionDto region,
    String capital,
    String flagUrl,
    BigDecimal latitude,
    BigDecimal longitude
) {
    public static CountryResponseDto from(Country country) {
        return new CountryResponseDto(
            country.getName(),
            country.getCode(),
            RegionDto.from(country.getRegion()),
            country.getCapital(),
            country.getFlags(),
            country.getLatitude(),
            country.getLongitude()
        );
    }
}
