package com.souzip.domain.souvenir.dto;

import java.util.List;

public record SouvenirNearbyListResponse(
        List<SouvenirNearbyResponse> souvenirs
) {
    public static SouvenirNearbyListResponse from(List<SouvenirNearbyResponse> souvenirs) {
        return new SouvenirNearbyListResponse(souvenirs);
    }
}
