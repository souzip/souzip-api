package com.souzip.api.domain.product.entity;

import lombok.Getter;
import java.util.Arrays;
import java.util.Optional;

@Getter
public enum Category {
    ELECTRONICS("전자기기"),
    FASHION("패션"),
    BEAUTY("뷰티"),
    HOME("홈"),
    SPORTS("스포츠"),
    TOYS("완구"),
    AUTOMOTIVE("자동차"),
    BOOKS("도서"),
    OTHERS("기타");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public static Optional<Category> from(String name) {
        if (isNullOrBlank(name)) return Optional.empty();
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(name))
                .findFirst();
    }

    private static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }
}
