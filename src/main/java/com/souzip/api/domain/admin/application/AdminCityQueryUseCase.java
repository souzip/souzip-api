package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.port.CityQueryPort.CityQueryResult;
import com.souzip.api.domain.admin.application.query.CitySearchQuery;
import com.souzip.api.global.common.dto.pagination.PaginationResponse;

public interface AdminCityQueryUseCase {

    PaginationResponse<CityQueryResult> getCities(CitySearchQuery query);
}
