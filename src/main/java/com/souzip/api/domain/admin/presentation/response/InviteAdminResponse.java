package com.souzip.api.domain.admin.presentation.response;

import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import java.util.UUID;

public record InviteAdminResponse(
    UUID adminId,
    String username,
    AdminRole role
) {
    public static InviteAdminResponse from(Admin admin) {
        return new InviteAdminResponse(
            admin.getId(),
            admin.getUsername().value(),
            admin.getRole()
        );
    }
}
