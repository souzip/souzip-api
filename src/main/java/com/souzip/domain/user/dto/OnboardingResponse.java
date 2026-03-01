package com.souzip.domain.user.dto;

import com.souzip.domain.category.dto.CategoryDto;
import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.entity.UserAgreement;
import java.util.List;

public record OnboardingResponse(
    String userId,
    String nickname,
    String profileImageUrl,
    List<CategoryDto> categories,
    UserAgreementInfo agreements
) {
    public static OnboardingResponse of(
        User user,
        List<CategoryDto> categories,
        UserAgreement agreement
    ) {
        return new OnboardingResponse(
            user.getUserId(),
            user.getNickname(),
            user.getProfileImageUrl(),
            categories,
            UserAgreementInfo.from(agreement)
        );
    }
}
