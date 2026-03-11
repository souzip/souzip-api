package com.souzip.application.admin.provided;

import com.souzip.domain.city.entity.City;
import com.souzip.domain.country.entity.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminLocationFinder {
    Page<City> getCities(Long countryId, String keyword, Pageable pageable);

    List<Country> getCountries(String keyword);
}
