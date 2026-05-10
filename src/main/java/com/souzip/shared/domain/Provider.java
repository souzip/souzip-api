package com.souzip.shared.domain;

import com.souzip.shared.exception.BusinessException;
import com.souzip.shared.exception.ErrorCode;

import java.util.Arrays;

public enum Provider {
    KAKAO,
    APPLE,
    GOOGLE;

    public static Provider from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Provider는 필수입니다.");
        }

        return Arrays.stream(values())
                .filter(provider -> provider.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT, "지원하지 않는 로그인 방식입니다: " + value));
    }
}
