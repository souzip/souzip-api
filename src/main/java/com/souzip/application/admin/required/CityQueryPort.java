package com.souzip.application.admin.required;

import com.souzip.domain.city.entity.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public interface CityQueryPort {
    Page<City> getCities(Long countryId, String keyword, Pageable pageable);
}