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

    private final String englishName;
    private final String koreanName;

    Region(String englishName, String koreanName) {
        this.englishName = englishName;
        this.koreanName = koreanName;
    }

    public static Optional<Region> from(String name) {
        if (isNullOrBlank(name)) {
            return Optional.empty();
        }

        return Arrays.stream(values())
            .filter(region -> region.englishName.equalsIgnoreCase(name))
            .findFirst();
    }

    private static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }
}
