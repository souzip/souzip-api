package com.souzip.api.domain.admin.infrastructure.persistence;

import com.souzip.api.domain.admin.model.AdminRefreshToken;
import com.souzip.api.domain.admin.repository.AdminRefreshTokenRepository;
import com.souzip.api.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaAuditing
@Import({AdminRefreshTokenRepositoryImpl.class, AdminRefreshTokenMapper.class, QuerydslConfig.class})
class AdminRefreshTokenRepositoryTest {

    @Autowired
    private AdminRefreshTokenRepository repository;

    @DisplayName("AdminRefreshToken 저장에 성공한다.")
    @Test
    void save_success() {
        // given
        UUID adminId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        AdminRefreshToken token = AdminRefreshToken.create(adminId, "refresh-token", expiresAt);

        // when
        AdminRefreshToken saved = repository.save(token);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAdminId()).isEqualTo(adminId);
        assertThat(saved.getToken()).isEqualTo("refresh-token");
    }

    @DisplayName("token으로 AdminRefreshToken 조회에 성공한다.")
    @Test
    void findByToken_success() {
        // given
        UUID adminId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        AdminRefreshToken token = AdminRefreshToken.create(adminId, "refresh-token", expiresAt);
        repository.save(token);

        // when
        Optional<AdminRefreshToken> found = repository.findByToken("refresh-token");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo("refresh-token");
    }

    @DisplayName("adminId로 AdminRefreshToken 조회에 성공한다.")
    @Test
    void findByAdminId_success() {
        // given
        UUID adminId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        AdminRefreshToken token = AdminRefreshToken.create(adminId, "refresh-token", expiresAt);
        repository.save(token);

        // when
        Optional<AdminRefreshToken> found = repository.findByAdminId(adminId);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getAdminId()).isEqualTo(adminId);
    }

    @DisplayName("AdminRefreshToken 삭제에 성공한다.")
    @Test
    void delete_success() {
        // given
        UUID adminId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        AdminRefreshToken token = AdminRefreshToken.create(adminId, "refresh-token", expiresAt);
        AdminRefreshToken saved = repository.save(token);

        // when
        repository.delete(saved);

        // then
        Optional<AdminRefreshToken> found = repository.findByToken("refresh-token");
        assertThat(found).isEmpty();
    }
}
