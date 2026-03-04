package com.souzip.domain.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NicknameCheckResponse {

    private final boolean available;
    private final String message;

    public static NicknameCheckResponse available() {
        return new NicknameCheckResponse(true, "사용 가능한 닉네임입니다.");
    }

    public static NicknameCheckResponse unavailable() {
        return new NicknameCheckResponse(false, "이미 사용 중인 닉네임입니다.");
    }
}
