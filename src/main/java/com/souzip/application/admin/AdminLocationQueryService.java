package com.souzip.application.admin;

import com.souzip.application.admin.provided.AdminLocationFinder;
import com.souzip.application.admin.required.CityQueryPort;
import com.souzip.application.admin.required.CountryQueryPort;
import com.souzip.domain.city.entity.City;
import com.souzip.domain.country.entity.Country;
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

    private final CityQueryPort cityQueryPort;
    private final CountryQueryPort countryQueryPort;

    @Override
    public Page<City> getCities(Long countryId, String keyword, Pageable pageable) {
        return cityQueryPort.getCities(countryId, keyword, pageable);
    }

    @Override
    public List<Country> getCountries(String keyword) {
        return countryQueryPort.getCountries(keyword);
    }
}