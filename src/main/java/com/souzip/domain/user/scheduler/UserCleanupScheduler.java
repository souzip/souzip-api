package com.souzip.domain.user.scheduler;

import com.souzip.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserService userService;

    @Scheduled(cron = "0 0 3 * * *")
    public void runUserCleanup() {
        log.info("탈퇴 유저 삭제 배치 스케줄러 실행 시작");
        long deletedCount = userService.deleteWithdrawnUsers();
        log.info("탈퇴 유저 {}명 삭제 완료", deletedCount);
    }
}
