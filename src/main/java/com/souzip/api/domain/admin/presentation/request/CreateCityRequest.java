package com.souzip.api.domain.admin.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCityRequest(
    @NotBlank(message = "도시 영문명을 입력해주세요.")
    String nameEn,

    @NotBlank(message = "도시 한글명을 입력해주세요.")
    String nameKr,

    @NotNull(message = "위도를 입력해주세요.")
    Double latitude,

    @NotNull(message = "경도를 입력해주세요.")
    Double longitude,

    @NotNull(message = "나라 ID를 입력해주세요.")
    Long countryId
) {}
