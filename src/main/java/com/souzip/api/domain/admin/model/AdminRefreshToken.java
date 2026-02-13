package com.souzip.api.domain.admin.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminRefreshToken {

    private final UUID id;
    private final UUID adminId;
    private String token;
    private LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    public static AdminRefreshToken create(UUID adminId, String token, LocalDateTime expiresAt) {
        return new AdminRefreshToken(
            UUID.randomUUID(),
            adminId,
            token,
            expiresAt,
            LocalDateTime.now()
        );
    }

    public static AdminRefreshToken restore(
        UUID id,
        UUID adminId,
        String token,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
    ) {
        return new AdminRefreshToken(id, adminId, token, expiresAt, createdAt);
    }
}
