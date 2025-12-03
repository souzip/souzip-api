package com.souzip.api.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private LoginUserInfo user;
    private boolean newUser;

    public static LoginResponse of(
        String accessToken,
        String refreshToken,
        LoginUserInfo user,
        boolean newUser
    ) {
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .user(user)
            .newUser(newUser)
            .build();
    }
}
