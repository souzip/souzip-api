package com.souzip.application.admin.required;

import com.souzip.domain.city.entity.CityCreateRequest;
import com.souzip.domain.city.entity.CityUpdateRequest;

public interface CityCommandPort {
    void createCity(CityCreateRequest request);

    void updateCity(Long cityId, CityUpdateRequest request);

    void deleteCity(Long cityId);

    void updateCityPriority(Long cityId, Integer priority);
}
