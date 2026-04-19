package com.souzip.domain.admin.infrastructure.persistence;

import com.souzip.domain.admin.model.AdminRefreshToken;
import com.souzip.domain.admin.repository.AdminRefreshTokenRepository;
import com.souzip.shared.config.QuerydslConfig;
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

    @DisplayName("만료된 AdminRefreshToken 일괄 삭제에 성공한다.")
    @Test
    void deleteAllByExpiresAtBefore_success() {
        // given
        UUID adminId1 = UUID.randomUUID();
        UUID adminId2 = UUID.randomUUID();
        UUID adminId3 = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        // 만료된 토큰 2개
        AdminRefreshToken expiredToken1 = AdminRefreshToken.create(adminId1, "expired-token-1", now.minusDays(1));
        AdminRefreshToken expiredToken2 = AdminRefreshToken.create(adminId2, "expired-token-2", now.minusDays(5));

        // 유효한 토큰 1개
        AdminRefreshToken validToken = AdminRefreshToken.create(adminId3, "valid-token", now.plusDays(30));

        repository.save(expiredToken1);
        repository.save(expiredToken2);
        repository.save(validToken);

        // when
        int deletedCount = repository.deleteAllByExpiresAtBefore(now);

        // then
        assertThat(deletedCount).isEqualTo(2);
        assertThat(repository.findByToken("expired-token-1")).isEmpty();
        assertThat(repository.findByToken("expired-token-2")).isEmpty();
        assertThat(repository.findByToken("valid-token")).isPresent();
    }
}
