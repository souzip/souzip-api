package com.souzip.api.domain.admin.application.command;

public record AdminUpdateCityCommand(
        Long cityId,
        String nameEn,
        String nameKr
) {
}
