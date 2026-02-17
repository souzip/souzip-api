package com.souzip.api.domain.country.application.query;

import com.souzip.api.domain.country.entity.Country;

public record CountryResult(
    Long id,
    String nameKr
) {
    public static CountryResult from(Country country) {
        return new CountryResult(
            country.getId(),
            country.getNameKr()
        );
    }
}
