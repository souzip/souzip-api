package com.souzip.api.adapter.integration.googleplaces.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GooglePlacesSearchResponse(
        List<Result> results,
        String status
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            String name,

            @JsonProperty("formatted_address")
            String formattedAddress,

            Geometry geometry
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(
            Location location
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(
            double lat,
            double lng
    ) {}
}
