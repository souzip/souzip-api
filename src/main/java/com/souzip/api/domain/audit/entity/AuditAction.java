package com.souzip.api.domain.audit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditAction {

    LOGIN("로그인", AuditCategory.USER),
    LOGOUT("로그아웃", AuditCategory.USER),
    WITHDRAW("회원탈퇴", AuditCategory.USER),

    MARKETING_CONSENT("마케팅 동의", AuditCategory.USER_AGREEMENT),

    SOUVENIR_CREATED("기념품 등록", AuditCategory.SOUVENIR),
    SOUVENIR_UPDATED("기념품 수정", AuditCategory.SOUVENIR),
    SOUVENIR_DELETED("기념품 삭제", AuditCategory.SOUVENIR),
    ;

    private final String description;
    private final AuditCategory category;
}
