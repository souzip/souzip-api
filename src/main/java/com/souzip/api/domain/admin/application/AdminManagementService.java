package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.command.InviteAdminCommand;
import com.souzip.api.domain.admin.exception.AdminErrorCode;
import com.souzip.api.domain.admin.exception.AdminException;
import com.souzip.api.domain.admin.infrastructure.encoder.AdminPasswordEncoderImpl;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminManagementService {

    private final AdminRepository adminRepository;
    private final AdminPasswordEncoderImpl passwordEncoder;

    @Transactional
    public Admin inviteAdmin(InviteAdminCommand command) {
        validateNotSuperAdmin(command.role());
        validateUsernameNotDuplicated(command.username());

        Admin admin = createAdmin(command);
        return adminRepository.save(admin);
    }

    private void validateNotSuperAdmin(AdminRole role) {
        if (role == AdminRole.SUPER_ADMIN) {
            throw new AdminException(AdminErrorCode.CANNOT_INVITE_SUPER_ADMIN);
        }
    }

    private void validateUsernameNotDuplicated(String username) {
        if (adminRepository.existsByUsername(username)) {
            throw new AdminException(AdminErrorCode.ADMIN_USERNAME_DUPLICATED);
        }
    }

    private Admin createAdmin(InviteAdminCommand command) {
        return Admin.create(
            command.username(),
            command.password(),
            command.role(),
            passwordEncoder
        );
    }
}
