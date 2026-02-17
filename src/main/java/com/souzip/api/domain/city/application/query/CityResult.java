package com.souzip.api.domain.city.application.query;

import com.souzip.api.domain.city.entity.City;

public record CityResult(
    Long id,
    String nameKr,
    String nameEn,
    Integer priority
) {
    public static CityResult from(City city) {
        return new CityResult(
            city.getId(),
            city.getNameKr(),
            city.getNameEn(),
            city.getPriority()
        );
    }
}
