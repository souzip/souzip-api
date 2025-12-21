package com.souzip.api.domain.souvenir.dto;

public record SouvenirNearbyResponse(
        Long id,
        String name,
        String categoryName,
        String thumbnail,
        Double distanceMeter
) {

    public static SouvenirNearbyResponse from(
            Long id,
            String name,
            String categoryName,
            String thumbnail,
            Double distanceMeter
    ) {
        return new SouvenirNearbyResponse(
                id,
                name,
                categoryName,
                thumbnail,
                distanceMeter
        );
    }
}
