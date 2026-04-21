package com.souzip.auth.application.exception;

import com.souzip.shared.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 Refresh Token입니다."),

    INVALID_TOKEN_FORMAT(HttpStatus.BAD_REQUEST, "토큰 형식이 올바르지 않습니다."),
    INVALID_JWT_STRUCTURE(HttpStatus.BAD_REQUEST, "JWT 구조가 올바르지 않습니다."),
    TOKEN_DECODE_FAILED(HttpStatus.BAD_REQUEST, "토큰 디코딩에 실패했습니다."),
    TOKEN_PARSE_FAILED(HttpStatus.BAD_REQUEST, "토큰 파싱에 실패했습니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다."),

    KAKAO_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 API 호출에 실패했습니다."),
    GOOGLE_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "구글 API 호출에 실패했습니다."),
    APPLE_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "애플 API 호출에 실패했습니다.");

    private final HttpStatus status;

    private final String message;
}