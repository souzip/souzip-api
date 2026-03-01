package com.souzip.domain.admin.presentation.response;

import com.souzip.domain.admin.application.AdminAuthService.RefreshResult;

public record AdminRefreshResponse(
    String accessToken,
    String refreshToken
) {
    public static AdminRefreshResponse from(RefreshResult result) {
        return new AdminRefreshResponse(
            result.accessToken(),
            result.refreshToken()
        );
    }
}
