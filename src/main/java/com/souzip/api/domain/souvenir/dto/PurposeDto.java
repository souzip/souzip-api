package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.souvenir.entity.Purpose;

public record PurposeDto(
        String name,
        String label
) {
    public static PurposeDto from(Purpose purpose) {
        return new PurposeDto(
                purpose.name(),
                purpose.getLabel()
        );
    }
}
