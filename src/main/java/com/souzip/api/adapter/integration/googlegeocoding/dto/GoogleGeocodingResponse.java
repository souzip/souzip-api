package com.souzip.api.adapter.integration.googlegeocoding.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleGeocodingResponse(
        List<Result> results,
        String status
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            @JsonProperty("address_components")
            List<AddressComponent> addressComponents,

            @JsonProperty("formatted_address")
            String formattedAddress
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressComponent(
            List<String> types,

            @JsonProperty("long_name")
            String longName,

            @JsonProperty("short_name")
            String shortName
    ) {}
}
