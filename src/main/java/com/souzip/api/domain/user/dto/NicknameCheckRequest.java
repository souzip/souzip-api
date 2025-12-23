package com.souzip.api.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameCheckRequest(

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 11, message = "닉네임은 2자 이상 11자 이하로 입력해주세요.")
    String nickname
) {}
