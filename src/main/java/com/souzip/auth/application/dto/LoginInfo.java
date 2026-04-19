package com.souzip.auth.application.dto;

public record LoginInfo(
        String accessToken,
        String refreshToken,
        boolean needsOnboarding,
        String userId,
        String nickname
) {
}