package com.souzip.api.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private LoginUserInfo user;
    private boolean needsOnboarding;

    public static LoginResponse of(
        String accessToken,
        String refreshToken,
        LoginUserInfo user,
        boolean needsOnboarding
    ) {
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .user(user)
            .needsOnboarding(needsOnboarding)
            .build();
    }
}
