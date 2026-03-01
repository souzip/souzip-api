package com.souzip.api.adapter.webapi.geocoding.dto;

public record GeocodingAddressResponse(
        String address,
        String city,
        String countryCode
) {}
