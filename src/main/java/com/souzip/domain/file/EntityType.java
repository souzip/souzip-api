package com.souzip.domain.file;

import java.util.Arrays;

public enum EntityType {
    NOTICE("Notice"),
    SOUVENIR("Souvenir");

    private final String value;

    EntityType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EntityType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 엔티티 타입입니다."));
    }
}
