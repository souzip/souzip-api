package com.souzip.domain.country.dto;

import com.souzip.domain.country.entity.Country;
import com.souzip.domain.currency.dto.CurrencyDto;
import java.math.BigDecimal;

public record CountryResponseDto(
    String nameEn,
    String nameKr,
    String code,
    RegionDto region,
    String capital,
    String imageUrl,
    BigDecimal latitude,
    BigDecimal longitude,
    CurrencyDto currency
) {
    public static CountryResponseDto from(Country country) {
        return new CountryResponseDto(
            country.getNameEn(),
            country.getNameKr(),
            country.getCode(),
            RegionDto.from(country.getRegion()),
            country.getCapital(),
            country.getImageUrl(),
            country.getLatitude(),
            country.getLongitude(),
            CurrencyDto.from(country.getCurrency())
        );
    }
}
