package com.souzip.api.domain.admin.event;

public record AdminCityCreateRequestedEvent(
    String nameEn,
    String nameKr,
    Double latitude,
    Double longitude,
    Long countryId
) {
    public static AdminCityCreateRequestedEvent of(
        String nameEn,
        String nameKr,
        Double latitude,
        Double longitude,
        Long countryId
    ) {
        return new AdminCityCreateRequestedEvent(nameEn, nameKr, latitude, longitude, countryId);
    }
}
