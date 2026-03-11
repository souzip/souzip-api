package com.souzip.domain.admin;

import com.souzip.domain.admin.exception.AdminErrorCode;
import com.souzip.domain.admin.exception.AdminException;

import static java.util.Objects.requireNonNull;

public record AdminRegisterRequest(
        String username,
        String password,
        AdminRole role
) {
    private static final int MIN_USERNAME_LENGTH = 2;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MIN_PASSWORD_LENGTH = 8;

    public AdminRegisterRequest {
        username = validateUsername(username);
        validatePassword(password);
        requireNonNull(role, "역할은 필수입니다.");
    }

    public static AdminRegisterRequest of(String username, String password, AdminRole role) {
        return new AdminRegisterRequest(username, password, role);
    }

    private static String validateUsername(String username) {
        requireNonNull(username, "아이디는 필수입니다.");

        String sanitized = username.trim();

        if (sanitized.length() < MIN_USERNAME_LENGTH || sanitized.length() > MAX_USERNAME_LENGTH) {
            throw new AdminException(AdminErrorCode.INVALID_USERNAME_LENGTH);
        }

        return sanitized;
    }

    private static void validatePassword(String password) {
        requireNonNull(password, "비밀번호는 필수입니다.");

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new AdminException(AdminErrorCode.INVALID_PASSWORD);
        }
    }
}