package com.souzip.api.adapter.webapi.location.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.souzip.api.application.location.dto.CitySearchResult;
import com.souzip.api.application.location.dto.SearchPlace;
import com.souzip.api.application.location.dto.PlaceSearchResult;
import com.souzip.api.application.location.dto.SearchResult;
import com.souzip.api.domain.city.entity.City;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchResponse(
        String type,
        String name,
        String country,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static List<SearchResponse> from(SearchResult result) {
        if (result instanceof CitySearchResult(List<City> cities)) {
            return cities.stream()
                    .map(SearchResponse::from)
                    .toList();
        }

        if (result instanceof PlaceSearchResult(List<SearchPlace> places)) {
            return places.stream()
                    .map(SearchResponse::from)
                    .toList();
        }

        return List.of();
    }

    private static SearchResponse from(City city) {
        return new SearchResponse(
                "city",
                city.getNameKr(),
                city.getCountry().getNameKr(),
                null,
                city.getLatitude(),
                city.getLongitude()
        );
    }

    private static SearchResponse from(SearchPlace place) {
        return new SearchResponse(
                "place",
                place.name(),
                null,
                place.address(),
                place.coordinate().latitude(),
                place.coordinate().longitude()
        );
    }
}
