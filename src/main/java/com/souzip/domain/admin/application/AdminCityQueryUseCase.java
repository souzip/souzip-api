package com.souzip.domain.admin.application;

import com.souzip.domain.admin.application.port.CityQueryPort.CityQueryResult;
import com.souzip.domain.admin.application.query.CitySearchQuery;
import com.souzip.global.common.dto.pagination.PaginationResponse;

public interface AdminCityQueryUseCase {

    PaginationResponse<CityQueryResult> getCities(CitySearchQuery query);
}
