package com.souzip.domain.admin;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminRefreshToken {

    private UUID id;

    private UUID adminId;

    private String token;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    public static AdminRefreshToken create(UUID adminId, String token, LocalDateTime expiresAt) {
        AdminRefreshToken refreshToken = new AdminRefreshToken();

        refreshToken.id = UUID.randomUUID();
        refreshToken.adminId = requireNonNull(adminId, "어드민 ID는 필수입니다.");
        refreshToken.token = requireNonNull(token, "토큰은 필수입니다.");
        refreshToken.expiresAt = requireNonNull(expiresAt, "만료일은 필수입니다.");
        refreshToken.createdAt = LocalDateTime.now();

        return refreshToken;
    }

    public void updateToken(String token, LocalDateTime expiresAt) {
        this.token = requireNonNull(token, "토큰은 필수입니다.");
        this.expiresAt = requireNonNull(expiresAt, "만료일은 필수입니다.");
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}