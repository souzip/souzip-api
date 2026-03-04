package com.souzip.domain.city.application.port;

import com.souzip.domain.city.application.command.CreateCityCommand;
import com.souzip.domain.city.application.command.DeleteCityCommand;
import com.souzip.domain.city.application.command.UpdateCityCommand;
import com.souzip.domain.city.application.command.UpdateCityPriorityCommand;

public interface CityManagementPort {

    void createCity(CreateCityCommand command);

    void deleteCity(DeleteCityCommand command);

    void updateCityPriority(UpdateCityPriorityCommand command);

    void updateCity(UpdateCityCommand command);
}
