package com.souzip.api.domain.admin.infrastructure.persistence;

import com.souzip.api.domain.admin.infrastructure.entity.AdminRefreshTokenEntity;
import com.souzip.api.domain.admin.model.AdminRefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdminRefreshTokenMapperTest {

    private AdminRefreshTokenMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AdminRefreshTokenMapper();
    }

    @DisplayName("AdminRefreshToken 도메인을 Entity로 변환에 성공한다.")
    @Test
    void toEntity_success() {
        // given
        UUID adminId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        AdminRefreshToken domain = AdminRefreshToken.create(adminId, "refresh-token", expiresAt);

        // when
        AdminRefreshTokenEntity entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getAdminId()).isEqualTo(adminId);
        assertThat(entity.getToken()).isEqualTo("refresh-token");
        assertThat(entity.getExpiresAt()).isEqualTo(expiresAt);
    }

    @DisplayName("Entity를 AdminRefreshToken 도메인으로 변환에 성공한다.")
    @Test
    void toDomain_success() {
        // given
        UUID id = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

        AdminRefreshTokenEntity entity = AdminRefreshTokenEntity.builder()
            .id(id)
            .adminId(adminId)
            .token("refresh-token")
            .expiresAt(expiresAt)
            .build();

        // when
        AdminRefreshToken domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getAdminId()).isEqualTo(adminId);
        assertThat(domain.getToken()).isEqualTo("refresh-token");
        assertThat(domain.getExpiresAt()).isEqualTo(expiresAt);
    }
}
