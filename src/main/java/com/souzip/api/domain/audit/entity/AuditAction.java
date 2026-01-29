package com.souzip.api.domain.audit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditAction {

    LOGIN("로그인", AuditCategory.USER),
    LOGOUT("로그아웃", AuditCategory.USER), ;

    private final String description;
    private final AuditCategory category;
}
