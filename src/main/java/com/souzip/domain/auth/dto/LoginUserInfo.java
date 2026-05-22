package com.souzip.domain.auth.dto;

import com.souzip.domain.user.entity.User;

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
