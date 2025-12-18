package com.souzip.api.domain.geocoding.dto;

public record GeocodingAddressResponse(
        String formattedAddress,
        String city,
        String countryCode
) {}
