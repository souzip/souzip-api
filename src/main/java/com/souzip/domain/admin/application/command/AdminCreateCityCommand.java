package com.souzip.domain.admin.application.command;

public record AdminCreateCityCommand(
    String nameEn,
    String nameKr,
    Double latitude,
    Double longitude,
    Long countryId
) {}
