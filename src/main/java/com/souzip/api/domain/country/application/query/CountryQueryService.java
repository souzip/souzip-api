package com.souzip.api.domain.country.application.query;

import com.souzip.api.domain.country.application.port.CountryAdminPort.CountryAdminResult;
import com.souzip.api.domain.country.repository.CountryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CountryQueryService {

    private final CountryRepository countryRepository;

    public List<CountryAdminResult> getCountries() {
        return countryRepository.findAll().stream()
            .map(CountryAdminResult::from)
            .toList();
    }
}
