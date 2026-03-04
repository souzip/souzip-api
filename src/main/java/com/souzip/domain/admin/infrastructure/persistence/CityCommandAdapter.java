package com.souzip.domain.admin.infrastructure.persistence;

import com.souzip.domain.admin.application.command.AdminCreateCityCommand;
import com.souzip.domain.admin.application.command.AdminDeleteCityCommand;
import com.souzip.domain.admin.application.command.AdminUpdateCityCommand;
import com.souzip.domain.admin.application.command.AdminUpdateCityPriorityCommand;
import com.souzip.domain.admin.application.port.CityCommandPort;
import com.souzip.domain.city.application.command.CreateCityCommand;
import com.souzip.domain.city.application.command.DeleteCityCommand;
import com.souzip.domain.city.application.command.UpdateCityCommand;
import com.souzip.domain.city.application.command.UpdateCityPriorityCommand;
import com.souzip.domain.city.application.port.CityManagementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CityCommandAdapter implements CityCommandPort {

    private final CityManagementPort cityManagementPort;

    @Override
    public void createCity(AdminCreateCityCommand adminCommand) {
        CreateCityCommand cityCommand = new CreateCityCommand(
                adminCommand.nameEn(),
                adminCommand.nameKr(),
                adminCommand.latitude(),
                adminCommand.longitude(),
                adminCommand.countryId()
        );
        cityManagementPort.createCity(cityCommand);
    }

    @Override
    public void updateCity(AdminUpdateCityCommand adminCommand) {
        UpdateCityCommand cityCommand = new UpdateCityCommand(
                adminCommand.cityId(),
                adminCommand.nameEn(),
                adminCommand.nameKr()
        );
        cityManagementPort.updateCity(cityCommand);
    }

    @Override
    public void deleteCity(AdminDeleteCityCommand adminCommand) {
        DeleteCityCommand cityCommand = new DeleteCityCommand(adminCommand.cityId());
        cityManagementPort.deleteCity(cityCommand);
    }

    @Override
    public void updateCityPriority(AdminUpdateCityPriorityCommand adminCommand) {
        UpdateCityPriorityCommand cityCommand = new UpdateCityPriorityCommand(
                adminCommand.cityId(),
                adminCommand.newPriority()
        );
        cityManagementPort.updateCityPriority(cityCommand);
    }
}
