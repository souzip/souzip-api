package com.souzip.api.domain.country.dto;

import java.util.List;

public record CountryListResponse(
    List<CountryResponseDto> countries
) {
    public static CountryListResponse from(List<CountryResponseDto> countries) {
        return new CountryListResponse(countries);
    }
}
