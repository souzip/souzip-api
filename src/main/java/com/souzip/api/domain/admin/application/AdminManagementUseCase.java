package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.AdminManagementService.AdminPageResult;
import com.souzip.api.domain.admin.application.command.AdminCreateCityCommand;
import com.souzip.api.domain.admin.application.command.AdminDeleteCityCommand;
import com.souzip.api.domain.admin.application.command.AdminUpdateCityPriorityCommand;
import com.souzip.api.domain.admin.application.command.InviteAdminCommand;
import com.souzip.api.domain.admin.model.Admin;
import java.util.UUID;

public interface AdminManagementUseCase {

    AdminPageResult getAdmins(int pageNo, int pageSize);

    Admin inviteAdmin(InviteAdminCommand command);

    void deleteAdmin(UUID adminId, UUID requesterId);

    void updateCityPriority(AdminUpdateCityPriorityCommand command);

    void createCity(AdminCreateCityCommand command);

    void deleteCity(AdminDeleteCityCommand command);
}
