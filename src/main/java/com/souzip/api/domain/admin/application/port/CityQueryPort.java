package com.souzip.api.domain.admin.application.port;

import java.time.LocalDateTime;
import java.util.List;

public interface CityQueryPort {
    List<CityQueryResult> getCities(Long countryId);

    record CityQueryResult(
        Long id,
        String nameKr,
        Integer priority,
        LocalDateTime updatedAt
    ) {}
}
