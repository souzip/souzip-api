package com.souzip.auth.application.dto;

public record RefreshInfo(
        String accessToken,
        String refreshToken
) {
}