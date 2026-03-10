package com.souzip.application.admin.dto;

import com.souzip.domain.admin.Admin;

public record AdminLoginResult(
        Admin admin,
        String accessToken,
        String refreshToken
) {
}