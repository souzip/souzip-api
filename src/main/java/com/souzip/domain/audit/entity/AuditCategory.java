package com.souzip.domain.audit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditCategory {

    USER("사용자"),
    USER_AGREEMENT("사용자 동의항목"),
    SOUVENIR("기념품")
    ;

    private final String description;
}
