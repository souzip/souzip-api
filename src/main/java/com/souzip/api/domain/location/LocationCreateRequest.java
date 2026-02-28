package com.souzip.api.domain.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LocationCreateRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotNull BigDecimal latitude,
        @NotNull BigDecimal longitude
) {
}
