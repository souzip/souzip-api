package com.souzip.adapter.webapi.admin.dto;

import com.souzip.application.admin.dto.AdminRefreshResult;

public record AdminRefreshResponse(
        String accessToken,
        String refreshToken
) {
    public static AdminRefreshResponse from(AdminRefreshResult result) {
        return new AdminRefreshResponse(
                result.accessToken(),
                result.refreshToken()
        );
    }
}