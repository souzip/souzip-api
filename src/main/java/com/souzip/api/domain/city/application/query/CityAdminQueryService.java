package com.souzip.api.domain.city.application.query;

import com.souzip.api.domain.city.application.port.CityAdminPort;
import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CityAdminQueryService implements CityAdminPort {

    private final CityRepository cityRepository;

    @Override
    public Page<CityAdminResult> getCities(Long countryId, String keyword, Pageable pageable) {
        return fetchCities(countryId, keyword, pageable)
            .map(CityAdminResult::from);
    }

    private Page<City> fetchCities(Long countryId, String keyword, Pageable pageable) {
        if (hasKeyword(keyword)) {
            return cityRepository.searchByKeyword(countryId, keyword.trim(), pageable);
        }
        return cityRepository.findByCountryIdWithPaging(countryId, pageable);
    }

    private boolean hasKeyword(String keyword) {
        return keyword != null && !keyword.isBlank();
    }
}
