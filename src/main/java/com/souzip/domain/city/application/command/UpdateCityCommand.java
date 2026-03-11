package com.souzip.domain.city.application.command;

public record UpdateCityCommand(
        Long cityId,
        String nameEn,
        String nameKr,
        Double latitude,
        Double longitude
) {
}
