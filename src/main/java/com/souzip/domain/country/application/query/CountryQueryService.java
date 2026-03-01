package com.souzip.domain.country.application.query;

import com.souzip.domain.country.application.port.CountryAdminPort.CountryAdminResult;
import com.souzip.domain.country.entity.Country;
import com.souzip.domain.country.repository.CountryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CountryQueryService {

    private final CountryRepository countryRepository;

    public List<CountryAdminResult> getCountries(String keyword) {
        if (hasNoKeyword(keyword)) {
            return toResults(countryRepository.findAllByOrderByNameKrAsc());
        }

        return toResults(countryRepository.findByKeywordOrderByNameKrAsc(keyword));
    }

    private boolean hasNoKeyword(String keyword) {
        return keyword == null || keyword.isBlank();
    }

    private List<CountryAdminResult> toResults(List<Country> countries) {
        return countries.stream()
                .map(CountryAdminResult::from)
                .toList();
    }
}
