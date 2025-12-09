package com.souzip.api.domain.auth.dto;

import com.souzip.api.domain.user.entity.User;

public record LoginUserInfo(
    String userId,
    String nickname
) {
    public static LoginUserInfo from(User user) {
        return new LoginUserInfo(
            user.getUserId(),
            user.getNickname()
        );
    }
}
