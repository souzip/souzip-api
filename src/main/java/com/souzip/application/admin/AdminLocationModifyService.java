package com.souzip.application.admin;

import com.souzip.application.admin.provided.AdminLocationModifier;
import com.souzip.application.admin.required.CityCommandPort;
import com.souzip.domain.city.entity.CityCreateRequest;
import com.souzip.domain.city.entity.CityUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class AdminLocationModifyService implements AdminLocationModifier {

    private final CityCommandPort cityCommandPort;

    @Override
    public void createCity(CityCreateRequest request) {
        cityCommandPort.createCity(request);
    }

    @Override
    public void updateCity(Long cityId, CityUpdateRequest request) {
        cityCommandPort.updateCity(cityId, request);
    }

    @Override
    public void deleteCity(Long cityId) {
        cityCommandPort.deleteCity(cityId);
    }

    @Override
    public void updateCityPriority(Long cityId, Integer priority) {
        cityCommandPort.updateCityPriority(cityId, priority);
    }
}