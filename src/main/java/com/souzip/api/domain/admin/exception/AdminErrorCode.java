package com.souzip.api.domain.admin.exception;

import com.souzip.api.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum AdminErrorCode implements BaseErrorCode {

    INVALID_USERNAME(HttpStatus.BAD_REQUEST, "아이디는 4자 이상 20자 이하여야 합니다."),
    ;

    private final HttpStatus status;
    private final String message;

    AdminErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
