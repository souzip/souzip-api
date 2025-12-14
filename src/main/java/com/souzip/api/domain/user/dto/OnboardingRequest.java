package com.souzip.api.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OnboardingRequest(
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 15, message = "닉네임은 15자 이하여야 합니다.")
    String nickname,

    String profileImageUrl,

    @NotEmpty(message = "최소 1개 이상의 카테고리를 선택해야 합니다.")
    @Size(min = 1)
    List<String> categories
) {}
