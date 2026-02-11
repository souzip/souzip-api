package com.souzip.api.domain.admin.model;

public record Username(String value) {

    public Username {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("아이디는 비어있을 수 없습니다.");
        }
        if (value.length() < 4 || value.length() > 20) {
            throw new IllegalArgumentException("아이디는 4자 이상 20자 이하여야 합니다.");
        }
    }
}
