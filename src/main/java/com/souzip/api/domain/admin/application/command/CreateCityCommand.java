package com.souzip.api.domain.admin.application.command;

public record CreateCityCommand(
    String nameEn,
    String nameKr,
    Double latitude,
    Double longitude,
    Long countryId
) {}
