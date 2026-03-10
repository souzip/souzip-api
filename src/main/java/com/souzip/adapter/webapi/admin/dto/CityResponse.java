package com.souzip.adapter.webapi.admin.dto;

import com.souzip.domain.city.entity.City;

import java.math.BigDecimal;

public record CityResponse(
        Long id,
        String nameEn,
        String nameKr,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer priority
) {
    public static CityResponse from(City city) {
        return new CityResponse(
                city.getId(),
                city.getNameEn(),
                city.getNameKr(),
                city.getLatitude(),
                city.getLongitude(),
                city.getPriority()
        );
    }
}