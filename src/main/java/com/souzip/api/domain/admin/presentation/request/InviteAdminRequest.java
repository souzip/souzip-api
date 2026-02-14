package com.souzip.api.domain.admin.presentation.request;

import com.souzip.api.domain.admin.application.command.InviteAdminCommand;
import com.souzip.api.domain.admin.model.AdminRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InviteAdminRequest(

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4-20자 사이여야 합니다.")
    @Pattern(regexp = USERNAME_PATTERN, message = "아이디는 영문, 숫자, 언더스코어만 가능합니다.")
    String username,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    String password,

    @NotNull(message = "역할은 필수입니다.")
    AdminRole role
) {
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";

    public InviteAdminCommand toCommand() {
        return new InviteAdminCommand(username, password, role);
    }
}
