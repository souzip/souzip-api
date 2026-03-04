package com.souzip.domain.admin.presentation.response;

import com.souzip.domain.admin.application.AdminAuthService.AdminLoginResult;
import com.souzip.domain.admin.model.AdminRole;

import java.util.UUID;

public record AdminLoginResponse(
    String accessToken,
    String refreshToken,
    UUID id,
    String username,
    AdminRole role
) {
    public static AdminLoginResponse from(AdminLoginResult result) {
        return new AdminLoginResponse(
            result.accessToken(),
            result.refreshToken(),
            result.admin().getId(),
            result.admin().getUsername().value(),
            result.admin().getRole()
        );
    }
}
