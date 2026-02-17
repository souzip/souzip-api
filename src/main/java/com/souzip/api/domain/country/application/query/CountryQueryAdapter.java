package com.souzip.api.domain.country.application.query;

import com.souzip.api.domain.admin.application.port.CountryQueryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CountryQueryAdapter implements CountryQueryPort {

    private final CountryQueryService countryQueryService;

    @Override
    public List<CountryQueryResult> getCountries() {
        return countryQueryService.getCountries().stream()
            .map(result -> new CountryQueryResult(
                result.id(),
                result.nameKr()
            ))
            .toList();
    }
}
