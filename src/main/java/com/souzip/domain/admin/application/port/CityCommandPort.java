package com.souzip.domain.admin.application.port;

import com.souzip.domain.admin.application.command.AdminCreateCityCommand;
import com.souzip.domain.admin.application.command.AdminDeleteCityCommand;
import com.souzip.domain.admin.application.command.AdminUpdateCityCommand;
import com.souzip.domain.admin.application.command.AdminUpdateCityPriorityCommand;

public interface CityCommandPort {

    void createCity(AdminCreateCityCommand command);

    void deleteCity(AdminDeleteCityCommand command);

    void updateCityPriority(AdminUpdateCityPriorityCommand command);

    void updateCity(AdminUpdateCityCommand command);
}
