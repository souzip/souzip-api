package com.souzip.application.admin;

import com.souzip.application.admin.provided.AdminLocationModifier;
import com.souzip.domain.city.application.command.*;
import com.souzip.domain.city.application.port.CityManagementPort;
import com.souzip.domain.city.entity.CityCreateRequest;
import com.souzip.domain.city.entity.CityUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class AdminLocationModifyService implements AdminLocationModifier {

    private final CityManagementPort cityManagementPort;

    @Override
    public void createCity(CityCreateRequest request) {
        cityManagementPort.createCity(new CreateCityCommand(
                request.nameEn(),
                request.nameKr(),
                request.coordinate().getLatitude().doubleValue(),
                request.coordinate().getLongitude().doubleValue(),
                request.countryId()
        ));
    }

    @Override
    public void updateCity(Long cityId, CityUpdateRequest request) {
        cityManagementPort.updateCity(new UpdateCityCommand(
                cityId,
                request.nameEn(),
                request.nameKr(),
                request.coordinate().getLatitude().doubleValue(),
                request.coordinate().getLongitude().doubleValue()
        ));
    }

    @Override
    public void deleteCity(Long cityId) {
        cityManagementPort.deleteCity(new DeleteCityCommand(cityId));
    }

    @Override
    public void updateCityPriority(Long cityId, Integer priority) {
        cityManagementPort.updateCityPriority(new UpdateCityPriorityCommand(cityId, priority));
    }
}
