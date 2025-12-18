package com.souzip.api.domain.user.dto;

import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.user.entity.User;

import java.util.List;

public record OnboardingResponse(
    String userId,
    String nickname,
    String profileImageUrl,
    List<CategoryDto> categories
) {
    public static OnboardingResponse of(User user, List<CategoryDto> categories) {
        return new OnboardingResponse(
            user.getUserId(),
            user.getNickname(),
            user.getProfileImageUrl(),
            categories
        );
    }

    public static OnboardingResponse from(User user) {
        List<CategoryDto> categoryDtos = user.getCategories().stream()
            .map(CategoryDto::from)
            .toList();

        return new OnboardingResponse(
            user.getUserId(),
            user.getNickname(),
            user.getProfileImageUrl(),
            categoryDtos
        );
    }
}
