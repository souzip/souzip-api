package com.souzip.adapter.webapi.admin.dto;

import com.souzip.domain.country.entity.Country;

public record CountryResponse(
        Long id,
        String nameKr
) {
    public static CountryResponse from(Country country) {
        return new CountryResponse(
                country.getId(),
                country.getNameKr()
        );
    }
}