package com.souzip.api.domain.city.event;

public record CityDeletedEvent(
    Long cityId
) {
    public static CityDeletedEvent of(Long cityId) {
        return new CityDeletedEvent(cityId);
    }
}
