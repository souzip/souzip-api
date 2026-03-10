package com.souzip.adapter.webapi.admin.dto;

import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.AdminRole;

import java.util.UUID;

public record AdminRegisterResponse(
        UUID adminId,
        String username,
        AdminRole role
) {
    public static AdminRegisterResponse from(Admin admin) {
        return new AdminRegisterResponse(
                admin.getId(),
                admin.getUsername(),
                admin.getRole()
        );
    }
}