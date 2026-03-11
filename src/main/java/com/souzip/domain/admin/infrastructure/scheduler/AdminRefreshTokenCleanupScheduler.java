package com.souzip.domain.admin.infrastructure.scheduler;

import com.souzip.domain.admin.repository.AdminRefreshTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminRefreshTokenCleanupScheduler {

    private final AdminRefreshTokenRepository adminRefreshTokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void cleanUpExpiredAdminRefreshTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = adminRefreshTokenRepository.deleteAllByExpiresAtBefore(now);
        log.info("[Admin Token Scheduler] 만료된 Admin Refresh Token {}개 삭제 완료 (실행 시각: {})",
            deletedCount, now);
    }
}
