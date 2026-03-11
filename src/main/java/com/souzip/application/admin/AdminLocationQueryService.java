package com.souzip.application.admin;

import com.souzip.application.admin.provided.AdminLocationFinder;
import com.souzip.domain.city.entity.City;
import com.souzip.domain.city.repository.CityRepository;
import com.souzip.domain.country.entity.Country;
import com.souzip.domain.country.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminLocationQueryService implements AdminLocationFinder {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

    @Override
    public Page<City> getCities(Long countryId, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return cityRepository.findByCountryIdWithPaging(countryId, pageable);
        }
        return cityRepository.searchByKeyword(countryId, keyword, pageable);
    }

    @Override
    public List<Country> getCountries(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return countryRepository.findAllByOrderByNameKrAsc();
        }
        return countryRepository.findByKeywordOrderByNameKrAsc(keyword);
    }
}
