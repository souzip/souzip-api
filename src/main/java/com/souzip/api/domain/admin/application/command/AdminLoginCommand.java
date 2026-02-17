package com.souzip.api.domain.admin.application.command;

public record AdminLoginCommand(
    String username,
    String password
) {}
