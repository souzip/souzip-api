package com.souzip.api.adapter.webapi.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.souzip.api.application.location.dto.AddressResult;

public record AddressResponse(
        @Deprecated
        @JsonProperty("formattedAddress")
        String formattedAddress,

        String address,
        String city,
        String countryCode
) {
    public static AddressResponse from(AddressResult result) {
        return new AddressResponse(
                result.address(),
                result.address(),
                result.city(),
                result.countryCode()
        );
    }
}
