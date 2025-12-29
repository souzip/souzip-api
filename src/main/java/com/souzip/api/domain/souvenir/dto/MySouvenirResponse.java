package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.souvenir.entity.Souvenir;

import java.time.LocalDateTime;

public record MySouvenirResponse(
    Long id,
    String thumbnailUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static MySouvenirResponse of(Souvenir souvenir, String thumbnailUrl) {
        return new MySouvenirResponse(
            souvenir.getId(),
            thumbnailUrl,
            souvenir.getCreatedAt(),
            souvenir.getUpdatedAt()
        );
    }
}
