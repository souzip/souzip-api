package com.souzip.api.domain.city.infrastructure.persistence;

import com.souzip.api.domain.city.application.port.CityAdminPort;
import com.souzip.api.domain.city.application.query.CityAdminQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CityAdminAdapter implements CityAdminPort {

    private final CityAdminQueryService cityAdminQueryService;

    @Override
    public Page<CityAdminResult> getCities(Long countryId, String keyword, Pageable pageable) {
        return cityAdminQueryService.getCities(countryId, keyword, pageable);
    }
}
