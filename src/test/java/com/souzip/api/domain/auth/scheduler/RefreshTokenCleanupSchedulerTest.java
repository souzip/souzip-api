package com.souzip.api.domain.auth.scheduler;

import com.souzip.api.domain.auth.entity.RefreshToken;
import com.souzip.api.domain.auth.repository.RefreshTokenRepository;
import com.souzip.api.domain.user.entity.Provider;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class RefreshTokenCleanupSchedulerTest {

    @Autowired
    private RefreshTokenCleanupScheduler scheduler;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("만료된 Refresh Token을 삭제한다.")
    void cleanupExpiredTokens() {
        // given
        User user = User.of(
            Provider.KAKAO,
            "test123",
            "유저",
            "수집"
        );
        userRepository.save(user);

        RefreshToken expiredToken = RefreshToken.of(
            user,
            "expired_token",
            LocalDateTime.now().minusDays(1)
        );
        refreshTokenRepository.save(expiredToken);

        RefreshToken validToken = RefreshToken.of(
            user,
            "valid_token",
            LocalDateTime.now().plusDays(10)
        );
        refreshTokenRepository.save(validToken);

        // when
        scheduler.cleanUpExpiredRefreshTokens();

        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(refreshTokenRepository.findById(expiredToken.getId())).isEmpty();
        assertThat(refreshTokenRepository.findById(validToken.getId())).isPresent();
    }
}
