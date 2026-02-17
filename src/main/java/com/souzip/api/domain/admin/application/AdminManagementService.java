package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.command.AdminCreateCityCommand;
import com.souzip.api.domain.admin.application.command.AdminDeleteCityCommand;
import com.souzip.api.domain.admin.application.command.AdminUpdateCityPriorityCommand;
import com.souzip.api.domain.admin.application.command.InviteAdminCommand;
import com.souzip.api.domain.admin.application.port.CityCommandPort;
import com.souzip.api.domain.admin.exception.AdminErrorCode;
import com.souzip.api.domain.admin.exception.AdminException;
import com.souzip.api.domain.admin.exception.AdminNotFoundException;
import com.souzip.api.domain.admin.infrastructure.encoder.AdminPasswordEncoderImpl;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.repository.AdminRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminManagementService implements AdminManagementUseCase {

    private final AdminRepository adminRepository;
    private final AdminPasswordEncoderImpl passwordEncoder;
    private final CityCommandPort cityCommandPort;

    @Override
    public AdminPageResult getAdmins(int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Admin> admins = adminRepository.findAllExcludingSuperAdmin(offset, pageSize);
        long total = adminRepository.countExcludingSuperAdmin();
        int totalPages = (int) Math.ceil((double) total / pageSize);

        return new AdminPageResult(admins, pageNo, pageSize, total, totalPages);
    }

    @Transactional
    @Override
    public Admin inviteAdmin(InviteAdminCommand command) {
        validateNotSuperAdmin(command.role());
        validateUsernameNotDuplicated(command.username());

        Admin admin = createAdmin(command);
        return adminRepository.save(admin);
    }

    @Transactional
    @Override
    public void deleteAdmin(UUID adminId, UUID requesterId) {
        Admin adminToDelete = adminRepository.findById(adminId)
            .orElseThrow(AdminNotFoundException::new);

        adminRepository.delete(adminToDelete);
    }

    @Transactional
    @Override
    public void updateCityPriority(AdminUpdateCityPriorityCommand command) {
        cityCommandPort.updateCityPriority(command);
    }

    @Transactional
    @Override
    public void createCity(AdminCreateCityCommand command) {
        cityCommandPort.createCity(command);
    }

    @Transactional
    @Override
    public void deleteCity(AdminDeleteCityCommand command) {
        cityCommandPort.deleteCity(command);
    }

    private void validateNotSuperAdmin(AdminRole role) {
        if (isSuperAdmin(role)) {
            throw new AdminException(AdminErrorCode.CANNOT_INVITE_SUPER_ADMIN);
        }
    }

    private boolean isSuperAdmin(AdminRole role) {
        return role == AdminRole.SUPER_ADMIN;
    }

    private void validateUsernameNotDuplicated(String username) {
        if (isUsernameDuplicated(username)) {
            throw new AdminException(AdminErrorCode.ADMIN_USERNAME_DUPLICATED);
        }
    }

    private boolean isUsernameDuplicated(String username) {
        return adminRepository.existsByUsername(username);
    }

    private Admin createAdmin(InviteAdminCommand command) {
        return Admin.create(
            command.username(),
            command.password(),
            command.role(),
            passwordEncoder
        );
    }

    public record AdminPageResult(
        List<Admin> admins,
        int pageNo,
        int pageSize,
        long total,
        int totalPages
    ) {}
}
