package com.souzip.application.location.dto;

public record AddressResult(
        String address,
        String city,
        String countryCode
) {
    public static AddressResult empty() {
        return new AddressResult(null, null, null);
    }
}
