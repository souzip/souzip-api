package com.souzip.api.domain.admin.application.port;

import java.util.List;

public interface CityQueryPort {
    List<CityQueryResult> getCities(Long countryId);

    record CityQueryResult(
        Long id,
        String nameKr,
        String nameEn,
        Integer priority
    ) {}
}
