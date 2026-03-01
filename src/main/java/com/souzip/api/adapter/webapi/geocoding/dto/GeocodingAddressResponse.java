package com.souzip.api.adapter.webapi.geocoding.dto;

import com.souzip.api.application.geocoding.dto.GeocodingResult;

public record GeocodingAddressResponse(
        String formattedAddress,
        String city,
        String countryCode
) {
    public static GeocodingAddressResponse from(GeocodingResult result) {
        return new GeocodingAddressResponse(
                result.formattedAddress(),
                result.city(),
                result.countryCode()
        );
    }
}
