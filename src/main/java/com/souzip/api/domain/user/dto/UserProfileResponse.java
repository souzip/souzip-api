package com.souzip.api.domain.user.dto;

import com.souzip.api.domain.user.entity.User;

public record UserProfileResponse(
    String userId,
    String nickname,
    String email,
    String profileImageUrl
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            user.getUserId(),
            user.getNickname(),
            user.getEmail(),
            user.getProfileImageUrl()
        );
    }
}
