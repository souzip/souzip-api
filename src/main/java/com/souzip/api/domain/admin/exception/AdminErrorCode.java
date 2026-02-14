package com.souzip.api.domain.admin.exception;

import com.souzip.api.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum AdminErrorCode implements BaseErrorCode {

    INVALID_USERNAME_EMPTY(HttpStatus.BAD_REQUEST, "아이디는 비어있을 수 없습니다."),
    INVALID_USERNAME_LENGTH(HttpStatus.BAD_REQUEST, "아이디는 4자 이상 20자 이하여야 합니다."),
    CANNOT_INVITE_SUPER_ADMIN(HttpStatus.BAD_REQUEST, "최고 관리자는 초대할 수 없습니다."),

    ADMIN_LOCKED(HttpStatus.UNAUTHORIZED, "잠긴 계정입니다. 관리자에게 문의하세요."),
    ADMIN_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다."),

    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "관리자를 찾을 수 없습니다."),

    ADMIN_USERNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
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
