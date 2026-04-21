package com.souzip.auth.scheduler;

import com.souzip.auth.adapter.integration.scheduler.RefreshTokenCleanupScheduler;
import com.souzip.auth.application.required.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupSchedulerTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenCleanupScheduler scheduler;

    @Test
    @DisplayName("만료된 Refresh Token을 삭제한다.")
    void cleanupExpiredTokens() {
        // when
        scheduler.cleanUpExpiredRefreshTokens();

        // then
        verify(refreshTokenRepository, times(1))
                .deleteAllExpiredBefore(any(LocalDateTime.class));
    }
}