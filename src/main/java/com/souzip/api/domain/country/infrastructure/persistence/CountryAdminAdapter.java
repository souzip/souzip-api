package com.souzip.api.domain.country.infrastructure.persistence;

import com.souzip.api.domain.country.application.port.CountryAdminPort;
import com.souzip.api.domain.country.application.query.CountryQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CountryAdminAdapter implements CountryAdminPort {

    private final CountryQueryService countryQueryService;

    @Override
    public List<CountryAdminResult> getCountries(String keyword) {
        return countryQueryService.getCountries(keyword);
    }
}
