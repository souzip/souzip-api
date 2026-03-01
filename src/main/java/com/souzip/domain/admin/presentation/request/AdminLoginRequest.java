package com.souzip.domain.admin.presentation.request;

import com.souzip.domain.admin.application.command.AdminLoginCommand;
import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(

    @NotBlank(message = "아이디는 필수입니다.")
    String username,

    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
) {
    public AdminLoginCommand toCommand() {
        return new AdminLoginCommand(username, password);
    }
}
