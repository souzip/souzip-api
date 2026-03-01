package com.souzip.domain.admin.application.command;

public record AdminUpdateCityCommand(
        Long cityId,
        String nameEn,
        String nameKr
) {
}
