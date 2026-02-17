package com.souzip.api.domain.admin.event;

public record AdminCityDeleteRequestedEvent(
    Long cityId
) {
    public static AdminCityDeleteRequestedEvent of(Long cityId) {
        return new AdminCityDeleteRequestedEvent(cityId);
    }
}
