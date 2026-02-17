package com.souzip.api.domain.country.application.query;

import com.souzip.api.domain.country.entity.Country;
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

    public List<CountryQueryResult> getCountries() {
        return countryRepository.findAll().stream()
            .map(CountryQueryResult::from)
            .toList();
    }

    public record CountryQueryResult(
        Long id,
        String nameKr
    ) {
        public static CountryQueryResult from(Country country) {
            return new CountryQueryResult(
                country.getId(),
                country.getNameKr()
            );
        }
    }
}
