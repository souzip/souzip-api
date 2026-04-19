package com.souzip.auth.adapter.integration.scheduler;

import com.souzip.auth.application.required.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void cleanUpExpiredRefreshTokens() {
        LocalDateTime now = LocalDateTime.now();

        int deletedCount = refreshTokenRepository.deleteAllExpiredBefore(now);

        log.info("[Token Scheduler] 만료된 Refresh Token {}개 삭제 완료 (실행 시각: {})", deletedCount, now);
    }
}