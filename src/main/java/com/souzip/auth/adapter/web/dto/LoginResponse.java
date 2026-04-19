package com.souzip.auth.adapter.web.dto;

import com.souzip.auth.application.dto.LoginInfo;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        boolean needsOnboarding,
        UserDto user
) {
    public record UserDto(String userId, String nickname) {
    }

    public static LoginResponse from(LoginInfo info) {
        return new LoginResponse(
                info.accessToken(),
                info.refreshToken(),
                info.needsOnboarding(),
                new UserDto(info.userId(), info.nickname())
        );
    }
}