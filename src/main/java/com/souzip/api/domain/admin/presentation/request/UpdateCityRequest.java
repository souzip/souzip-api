package com.souzip.api.domain.admin.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateCityRequest(
        @NotBlank(message = "영문 도시명을 입력해주세요.")
        String nameEn,

        @NotBlank(message = "한글 도시명을 입력해주세요.")
        String nameKr
) {
}
