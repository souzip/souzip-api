package com.souzip.domain.city.entity;

import com.souzip.domain.shared.Coordinate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CityUpdateRequest(
        @NotBlank(message = "도시 영문명은 필수입니다.")
        String nameEn,

        @NotBlank(message = "도시 한글명은 필수입니다.")
        String nameKr,

        @Valid
        @NotNull(message = "좌표는 필수입니다.")
        Coordinate coordinate
) {
}