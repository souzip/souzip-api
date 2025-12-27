package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.souvenir.entity.Purpose;

import java.math.BigDecimal;

public record SouvenirNearbyResponse(
        Long id,
        String name,
        Category category,
        Purpose purpose,
        int localPrice,
        int krwPrice,
        String currencySymbol,
        String thumbnail,
        BigDecimal latitude,
        BigDecimal longitude,
        String address
) {
    public static SouvenirNearbyResponse from(
            Long id,
            String name,
            Category category,
            Purpose purpose,
            int localPrice,
            int krwPrice,
            String currencySymbol,
            String thumbnail,
            BigDecimal latitude,
            BigDecimal longitude,
            String address
    ) {
        return new SouvenirNearbyResponse(
                id,
                name,
                category,
                purpose,
                localPrice,
                krwPrice,
                currencySymbol,
                thumbnail,
                latitude,
                longitude,
                address
        );
    }
}
