package com.souzip.api.domain.city.application.command;

public record UpdateCityCommand(
        Long cityId,
        String nameEn,
        String nameKr
) {
}
