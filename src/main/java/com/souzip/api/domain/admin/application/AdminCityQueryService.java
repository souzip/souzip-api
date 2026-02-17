package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.port.CityQueryPort;
import com.souzip.api.domain.admin.application.port.CityQueryPort.CityQueryResult;
import com.souzip.api.domain.admin.application.query.CitySearchQuery;
import com.souzip.api.global.common.dto.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminCityQueryService implements AdminCityQueryUseCase {

    private final CityQueryPort cityQueryPort;

    @Override
    public PaginationResponse<CityQueryResult> getCities(CitySearchQuery query) {
        return cityQueryPort.getCities(
            query.countryId(), query.keyword(), query.pageNo(), query.pageSize()
        );
    }
}
