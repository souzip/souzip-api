package com.souzip.domain.admin.infrastructure.persistence;

import com.souzip.domain.admin.application.port.CountryQueryPort;
import com.souzip.domain.country.application.port.CountryAdminPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CountryQueryAdapter implements CountryQueryPort {

    private final CountryAdminPort countryAdminPort;

    @Override
    public List<CountryQueryResult> getCountries(String keyword) {
        return countryAdminPort.getCountries(keyword).stream()
            .map(c -> new CountryQueryResult(
                c.id(),
                c.nameKr()
            ))
            .toList();
    }
}
