package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.port.CountryQueryPort;
import com.souzip.api.domain.admin.application.port.CountryQueryPort.CountryQueryResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminCountryQueryUseCase {

    private final CountryQueryPort countryQueryPort;

    public List<CountryQueryResult> getCountries() {
        return countryQueryPort.getCountries();
    }
}
