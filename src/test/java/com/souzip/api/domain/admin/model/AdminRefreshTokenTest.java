package com.souzip.api.domain.admin.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdminRefreshTokenTest {

    @DisplayName("AdminRefreshToken 생성에 성공한다.")
    @Test
    void create_success() {
        // given
        UUID adminId = UUID.randomUUID();
        String token = "refresh-token";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

        // when
        AdminRefreshToken refreshToken = AdminRefreshToken.create(adminId, token, expiresAt);

        // then
        assertThat(refreshToken.getId()).isNotNull();
        assertThat(refreshToken.getAdminId()).isEqualTo(adminId);
        assertThat(refreshToken.getToken()).isEqualTo(token);
        assertThat(refreshToken.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(refreshToken.getCreatedAt()).isNotNull();
    }

    @DisplayName("AdminRefreshToken 복원에 성공한다.")
    @Test
    void restore_success() {
        // given
        UUID id = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String token = "refresh-token";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        LocalDateTime createdAt = LocalDateTime.now();

        // when
        AdminRefreshToken refreshToken = AdminRefreshToken.restore(
            id, adminId, token, expiresAt, createdAt
        );

        // then
        assertThat(refreshToken.getId()).isEqualTo(id);
        assertThat(refreshToken.getAdminId()).isEqualTo(adminId);
        assertThat(refreshToken.getToken()).isEqualTo(token);
        assertThat(refreshToken.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(refreshToken.getCreatedAt()).isEqualTo(createdAt);
    }
}


