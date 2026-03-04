package com.souzip.domain.category.entity;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

@Getter
public enum Category {

    FOOD_SNACK("먹거리·간식"),
    BEAUTY_HEALTH("뷰티·헬스"),
    FASHION_ACCESSORY("패션·악세서리"),
    CULTURE_TRADITION("문화·전통"),
    TOY_KIDS("장난감·키즈"),
    SOUVENIR_BASIC("기념품 기본템"),
    HOME_LIFESTYLE("홈·라이프스타일"),
    STATIONERY_ART("문구·아트"),
    TRAVEL_PRACTICAL("여행·실용템"),
    TECH_GADGET("테크·전자제품");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public static Optional<Category> from(String name) {
        if (isNullOrBlank(name)) return Optional.empty();

        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(name)
                    || c.label.equalsIgnoreCase(name))
                .findFirst();
    }

    private static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }
}
