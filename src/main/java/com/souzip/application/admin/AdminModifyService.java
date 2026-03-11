package com.souzip.application.admin;

import com.souzip.application.admin.provided.AdminFinder;
import com.souzip.application.admin.provided.AdminModifier;
import com.souzip.application.admin.required.AdminRepository;
import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.AdminRegisterRequest;
import com.souzip.domain.admin.AdminRole;
import com.souzip.domain.admin.PasswordEncoder;
import com.souzip.domain.admin.exception.AdminErrorCode;
import com.souzip.domain.admin.exception.AdminException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminModifyService implements AdminModifier {

    private final AdminRepository adminRepository;
    private final AdminFinder adminFinder;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public Admin register(AdminRegisterRequest request) {
        if (request.role() == AdminRole.SUPER_ADMIN) {
            throw new AdminException(AdminErrorCode.CANNOT_INVITE_SUPER_ADMIN);
        }

        if (adminRepository.existsByUsername(request.username())) {
            throw new AdminException(AdminErrorCode.ADMIN_USERNAME_DUPLICATED);
        }

        return adminRepository.save(Admin.register(request, passwordEncoder));
    }

    @Transactional
    @Override
    public void delete(UUID adminId, UUID requesterId) {
        Admin admin = adminFinder.findById(adminId);

        adminRepository.delete(admin);
    }
}