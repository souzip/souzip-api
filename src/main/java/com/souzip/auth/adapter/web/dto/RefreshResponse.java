package com.souzip.auth.adapter.web.dto;

import com.souzip.auth.application.dto.RefreshInfo;

public record RefreshResponse(
        String accessToken,
        String refreshToken
) {
    public static RefreshResponse from(RefreshInfo info) {
        return new RefreshResponse(
                info.accessToken(),
                info.refreshToken()
        );
    }
}