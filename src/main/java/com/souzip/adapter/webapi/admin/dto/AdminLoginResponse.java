package com.souzip.adapter.webapi.admin.dto;

import com.souzip.application.admin.dto.AdminLoginResult;
import com.souzip.domain.admin.AdminRole;

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
                result.admin().getUsername(),
                result.admin().getRole()
        );
    }
}