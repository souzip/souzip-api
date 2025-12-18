package com.souzip.api.domain.user.dto;

import java.util.Set;

public record ProfileColorsResponse(
    Set<String> colors
) {
    public static ProfileColorsResponse of(Set<String> colors) {
        return new ProfileColorsResponse(colors);
    }
}
