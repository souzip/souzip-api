package com.souzip.domain.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminRefreshTokenTest {

    UUID adminId;
    String token;
    LocalDateTime expiresAt;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        token = "refresh-token-value";
        expiresAt = LocalDateTime.now().plusDays(30);
    }

    @Test
    void create() {
        AdminRefreshToken refreshToken = AdminRefreshToken.create(adminId, token, expiresAt);

        assertThat(refreshToken.getId()).isNotNull();
        assertThat(refreshToken.getAdminId()).isEqualTo(adminId);
        assertThat(refreshToken.getToken()).isEqualTo(token);
        assertThat(refreshToken.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(refreshToken.getCreatedAt()).isNotNull();
    }

    @Test
    void createNullAdminIdFail() {
        assertThatThrownBy(() -> AdminRefreshToken.create(null, token, expiresAt))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createNullTokenFail() {
        assertThatThrownBy(() -> AdminRefreshToken.create(adminId, null, expiresAt))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createNullExpiresAtFail() {
        assertThatThrownBy(() -> AdminRefreshToken.create(adminId, token, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateToken() {
        AdminRefreshToken refreshToken = AdminRefreshToken.create(adminId, token, expiresAt);
        String newToken = "new-refresh-token";
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(30);

        refreshToken.updateToken(newToken, newExpiresAt);

        assertThat(refreshToken.getToken()).isEqualTo(newToken);
        assertThat(refreshToken.getExpiresAt()).isEqualTo(newExpiresAt);
    }

    @Test
    void isExpired() {
        AdminRefreshToken expiredToken = AdminRefreshToken.create(
                adminId, token, LocalDateTime.now().minusDays(1)
        );

        assertThat(expiredToken.isExpired()).isTrue();
    }

    @Test
    void isNotExpired() {
        AdminRefreshToken validToken = AdminRefreshToken.create(
                adminId, token, LocalDateTime.now().plusDays(30)
        );

        assertThat(validToken.isExpired()).isFalse();
    }
}