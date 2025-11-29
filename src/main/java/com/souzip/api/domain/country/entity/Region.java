package com.souzip.api.domain.country.entity;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

@Getter
public enum Region {

    AFRICA("Africa", "아프리카"),
    AMERICAS("Americas", "아메리카"),
    ASIA("Asia", "아시아"),
    EUROPE("Europe", "유럽"),
    OCEANIA("Oceania", "오세아니아"),
    ANTARCTIC("Antarctic", "남극");

    private final String code;
    private final String displayName;

    Region(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static Optional<Region> fromCode(String code) {
        if (isNullOrBlank(code)) {
            return Optional.empty();
        }

        return Arrays.stream(values())
            .filter(region -> region.code.equalsIgnoreCase(code))
            .findFirst();
    }

    private static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }
}
