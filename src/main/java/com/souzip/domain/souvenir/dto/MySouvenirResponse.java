package com.souzip.domain.souvenir.dto;

import com.souzip.domain.souvenir.entity.Souvenir;

import java.time.LocalDateTime;

public record MySouvenirResponse(
        Long id,
        String thumbnailUrl,
        String countryCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long wishlistCount,
        boolean isWishlisted
) {
    public static MySouvenirResponse of(Souvenir souvenir, String thumbnailUrl, boolean isWishlisted, long wishlistCount) {
        return new MySouvenirResponse(
                souvenir.getId(),
                thumbnailUrl,
                souvenir.getCountryCode(),
                souvenir.getCreatedAt(),
                souvenir.getUpdatedAt(),
                wishlistCount,
                isWishlisted
        );
    }
}
