package com.souzip.api.domain.audit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditCategory {

    USER("사용자"),
    SOUVENIR("기념품")
    ;

    private final String description;
}
