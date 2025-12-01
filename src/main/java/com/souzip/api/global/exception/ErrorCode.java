package com.souzip.api.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 국가 정보를 찾을 수 없습니다."),
    COUNTRY_REGION_INVALID(HttpStatus.BAD_REQUEST, "국가의 지역 코드가 유효하지 않습니다."),

    EXCHANGE_RATE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 환율 정보를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
