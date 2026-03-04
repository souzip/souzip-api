package com.souzip.domain.souvenir.entity;

import lombok.Getter;
import java.util.Arrays;
import java.util.Optional;

@Getter
public enum Purpose {
    GIFT("선물용"),
    PERSONAL("개인용");

    private final String label;

    Purpose(String label) {
        this.label = label;
    }

    public static Optional<Purpose> from(String text) {
        if (isNullOrBlank(text)) return Optional.empty();

        return Arrays.stream(values())
                .filter(p -> p.label.equalsIgnoreCase(text))
                .findFirst();
    }

    private static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }
}
