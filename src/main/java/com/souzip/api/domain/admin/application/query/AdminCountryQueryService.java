package com.souzip.api.domain.admin.application.query;

import com.souzip.api.domain.admin.application.AdminCountryQueryUseCase;
import com.souzip.api.domain.admin.application.port.CountryQueryPort;
import com.souzip.api.domain.admin.application.port.CountryQueryPort.CountryQueryResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminCountryQueryService implements AdminCountryQueryUseCase {

    private final CountryQueryPort countryQueryPort;

    @Override
    public List<CountryQueryResult> getCountries(String keyword) {
        return countryQueryPort.getCountries(keyword);
    }
}
