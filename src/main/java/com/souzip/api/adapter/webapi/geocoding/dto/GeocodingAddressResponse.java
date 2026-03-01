package com.souzip.api.adapter.webapi.geocoding.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.souzip.api.application.geocoding.dto.GeocodingResult;

public record GeocodingAddressResponse(
        @Deprecated
        @JsonProperty("formattedAddress")
        String formattedAddress,

        String address,
        String city,
        String countryCode
) {
    public static GeocodingAddressResponse from(GeocodingResult result) {
        return new GeocodingAddressResponse(
                result.address(),
                result.address(),
                result.city(),
                result.countryCode()
        );
    }
}
