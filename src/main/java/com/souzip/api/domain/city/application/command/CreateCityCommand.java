package com.souzip.api.domain.city.application.command;

public record CreateCityCommand(
    String nameEn,
    String nameKr,
    Double latitude,
    Double longitude,
    Long countryId
) {}
