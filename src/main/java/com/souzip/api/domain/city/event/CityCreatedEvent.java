package com.souzip.api.domain.city.event;

public record CityCreatedEvent(
    Long cityId,
    Long countryId
) {
    public static CityCreatedEvent of(Long cityId, Long countryId) {
        return new CityCreatedEvent(cityId, countryId);
    }
}
