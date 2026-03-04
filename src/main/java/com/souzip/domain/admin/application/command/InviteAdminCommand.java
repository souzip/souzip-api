package com.souzip.domain.admin.application.command;

import com.souzip.domain.admin.model.AdminRole;

public record InviteAdminCommand(
    String username,
    String password,
    AdminRole role
) {}
