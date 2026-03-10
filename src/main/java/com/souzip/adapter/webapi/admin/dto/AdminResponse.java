package com.souzip.adapter.webapi.admin.dto;

import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.AdminRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminResponse(
        UUID id,
        String username,
        AdminRole role,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
    public static AdminResponse from(Admin admin) {
        return new AdminResponse(
                admin.getId(),
                admin.getUsername(),
                admin.getRole(),
                admin.getLastLoginAt(),
                admin.getCreatedAt()
        );
    }
}