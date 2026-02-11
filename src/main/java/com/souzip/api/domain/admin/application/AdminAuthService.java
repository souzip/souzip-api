package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.command.AdminLoginCommand;
import com.souzip.api.domain.admin.exception.AdminLockedException;
import com.souzip.api.domain.admin.exception.AdminLoginFailedException;
import com.souzip.api.domain.admin.exception.AdminNotFoundException;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminPasswordEncoder;
import com.souzip.api.domain.admin.model.Username;
import com.souzip.api.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final AdminPasswordEncoder passwordEncoder;

    @Transactional
    public Admin login(AdminLoginCommand command) {
        Admin admin = adminRepository.findByUsername(new Username(command.username()))
            .orElseThrow(AdminNotFoundException::new);

        validateNotLocked(admin);
        validatePassword(admin, command.password());

        admin.recordLoginSuccess();
        return adminRepository.save(admin);
    }

    private void validateNotLocked(Admin admin) {
        if (admin.isLocked()) {
            throw new AdminLockedException();
        }
    }

    private void validatePassword(Admin admin, String password) {
        if (isPasswordMismatch(admin, password)) {
            handleLoginFailure(admin);
        }
    }

    private boolean isPasswordMismatch(Admin admin, String password) {
        return !admin.matchesPassword(password, passwordEncoder);
    }

    private void handleLoginFailure(Admin admin) {
        admin.recordLoginFailure();
        adminRepository.save(admin);
        throw new AdminLoginFailedException();
    }
}
