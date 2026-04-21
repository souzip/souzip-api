package com.souzip.auth.application.dto;

public record UserInfo(
        Long id,
        String userId,
        String nickname,
        boolean needsOnboarding
) {
}