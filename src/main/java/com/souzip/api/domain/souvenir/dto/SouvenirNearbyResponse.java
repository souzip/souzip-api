package com.souzip.api.domain.souvenir.dto;

public record SouvenirNearbyResponse(
        Long id,
        String name,
        String categoryName,
        String thumbnail,
        int distanceMeter
) {

    public static SouvenirNearbyResponse from(
            Long id,
            String name,
            String categoryName,
            String thumbnail,
            int distanceMeter
    ) {
        return new SouvenirNearbyResponse(
                id,
                name,
                categoryName,
                thumbnail,
                (int) Math.round(distanceMeter)
        );
    }
}
