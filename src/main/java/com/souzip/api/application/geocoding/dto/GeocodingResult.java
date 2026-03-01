package com.souzip.api.application.geocoding.dto;

public record GeocodingResult(
        String formattedAddress,
        String city,
        String countryCode
) {
    public static GeocodingResult empty() {
        return new GeocodingResult(null, null, null);
    }
}
