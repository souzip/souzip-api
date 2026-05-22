package com.souzip.domain.auth.dto;

public record RefreshResponse(
    String accessToken,
    String refreshToken
) {
    public static RefreshResponse of(String accessToken, String refreshToken) {
        return new RefreshResponse(accessToken, refreshToken);
    }
}
