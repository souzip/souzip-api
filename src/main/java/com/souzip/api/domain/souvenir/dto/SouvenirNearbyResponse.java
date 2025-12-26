package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.category.dto.CategoryDto;
import java.math.BigDecimal;

public record SouvenirNearbyResponse(
        Long id,
        String name,
        CategoryDto categoryDto,
        PurposeDto purposeDto,
        int localPrice,
        int krwPrice,
        String currencySymbol,
        String thumbnail,
        int distanceMeter,
        BigDecimal latitude,
        BigDecimal longitude,
        String address
) {
    public static SouvenirNearbyResponse from(
            Long id,
            String name,
            CategoryDto categoryDto,
            PurposeDto purposeDto,
            int localPrice,
            int krwPrice,
            String currencySymbol,
            String thumbnail,
            double distanceMeter,
            BigDecimal latitude,
            BigDecimal longitude,
            String address
    ) {
        return new SouvenirNearbyResponse(
                id,
                name,
                categoryDto,
                purposeDto,
                localPrice,
                krwPrice,
                currencySymbol,
                thumbnail,
                (int) Math.round(distanceMeter),
                latitude,
                longitude,
                address
        );
    }
}
