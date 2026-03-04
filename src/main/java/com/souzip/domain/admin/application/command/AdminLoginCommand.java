package com.souzip.domain.admin.application.command;

public record AdminLoginCommand(
    String username,
    String password
) {}
