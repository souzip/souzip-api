package com.souzip.api.domain.audit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditActionType {
    USER_SIGNUP("회원가입"),
    USER_LOGIN("로그인"),
    USER_LOGOUT("로그아웃"),
    USER_DEACTIVATE("회원 탈퇴");

    private final String description;
}
