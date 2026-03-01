package com.souzip.api.adapter.webapi.geocoding.dto;

import com.souzip.api.application.geocoding.dto.GeocodingResult;

public record GeocodingAddressResponse(
        String address,
        String city,
        String countryCode
) {
    public static GeocodingAddressResponse from(GeocodingResult result) {
        return new GeocodingAddressResponse(
                result.address(),
                result.city(),
                result.countryCode()
        );
    }
}
