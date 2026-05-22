package com.souzip.domain.admin.application.port;

import com.souzip.global.common.dto.pagination.PaginationResponse;
import java.time.LocalDateTime;

public interface CityQueryPort {

    PaginationResponse<CityQueryResult> getCities(Long countryId, String keyword, int pageNo, int pageSize);

    record CityQueryResult(
            Long id,
            String nameKr,
            String nameEn,
            Integer priority,
            LocalDateTime updatedAt
    ) {}
}
