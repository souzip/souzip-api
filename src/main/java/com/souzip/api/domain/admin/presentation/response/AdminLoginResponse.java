package com.souzip.api.domain.admin.presentation.response;

import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminLoginResponse(
    UUID id,
    String username,
    AdminRole role,
    LocalDateTime lastLoginAt
) {
    public static AdminLoginResponse from(Admin admin) {
        return new AdminLoginResponse(
            admin.getId(),
            admin.getUsername().value(),
            admin.getRole(),
            admin.getLastLoginAt()
        );
    }
}
