package com.souzip.adapter.scheduler;


import com.souzip.application.admin.required.AdminRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
