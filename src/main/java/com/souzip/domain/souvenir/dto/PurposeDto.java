package com.souzip.domain.souvenir.dto;

import com.souzip.domain.souvenir.entity.Purpose;

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
