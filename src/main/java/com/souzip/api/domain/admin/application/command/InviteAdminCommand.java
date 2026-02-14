package com.souzip.api.domain.admin.application.command;

import com.souzip.api.domain.admin.model.AdminRole;

public record InviteAdminCommand(
    String username,
    String password,
    AdminRole role
) {
}
