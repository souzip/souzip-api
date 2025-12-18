package com.souzip.api.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OnboardingRequest(
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 11, message = "닉네임은 11자 이하여야 합니다.")
    String nickname,

    String profileImageColor,

    @NotEmpty(message = "최소 1개 이상의 카테고리를 선택해야 합니다.")
    @Size(min = 1, max = 5, message = "카테고리는 최대 5개까지 선택할 수 있습니다.")
    List<String> categories
) {}
