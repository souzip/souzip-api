package com.souzip.domain.migration.apple.service;

import com.souzip.domain.user.entity.Provider;
import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.repository.UserRepository;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleMigrationPrepService {

    private final UserRepository userRepository;
    private final AppleTransferService appleTransferService;

    private static final int RATE_LIMIT_DELAY_MILLIS = 100;

    @Transactional
    public MigrationResult prepareAllAppleUsers() {
        log.info("=== Apple 사용자 마이그레이션 준비 시작 ===");

        List<User> appleUsers = findAllAppleUsers();

        if (hasNoAppleUsers(appleUsers)) {
            throw new BusinessException(ErrorCode.NO_APPLE_USERS_FOUND);
        }

        MigrationCounters counters = processAllUsers(appleUsers);

        return logAndReturnResult(appleUsers.size(), counters);
    }

    private List<User> findAllAppleUsers() {
        List<User> appleUsers = userRepository.findByProvider(Provider.APPLE);
        log.info("총 {} 명의 Apple 사용자 발견", appleUsers.size());
        return appleUsers;
    }

    private boolean hasNoAppleUsers(List<User> appleUsers) {
        return appleUsers.isEmpty();
    }

    private MigrationCounters processAllUsers(List<User> appleUsers) {
        MigrationCounters counters = new MigrationCounters();
        appleUsers.forEach(user -> processUser(user, counters));
        return counters;
    }

    private void processUser(User user, MigrationCounters counters) {
        try {
            if (alreadyHasTransferIdentifier(user)) {
                handleAlreadyMigrated(user, counters);
                return;
            }

            attemptMigration(user, counters);
            waitForRateLimit();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            handleMigrationFailure(user, counters, e);
        }
    }

    private boolean alreadyHasTransferIdentifier(User user) {
        return user.getTransferIdentifier() != null;
    }

    private void handleAlreadyMigrated(User user, MigrationCounters counters) {
        log.info("이미 저장됨 - userId: {}, transferId: {}",
            user.getId(), user.getTransferIdentifier());
        counters.incrementSkip();
    }

    private void attemptMigration(User user, MigrationCounters counters) {
        String transferIdentifier = appleTransferService.getTransferIdentifier(user.getProviderId());

        if (hasTransferIdentifier(transferIdentifier)) {
            saveMigration(user, transferIdentifier, counters);
        } else {
            handleNoTransferIdentifier(user, counters);
        }
    }

    private boolean hasTransferIdentifier(String transferIdentifier) {
        return transferIdentifier != null;
    }

    private void saveMigration(User user, String transferIdentifier, MigrationCounters counters) {
        user.updateTransferIdentifier(transferIdentifier);
        userRepository.save(user);
        counters.incrementSuccess();

        log.info("저장 성공 - userId: {}, providerId: {}, transferId: {}",
            user.getId(), user.getProviderId(), transferIdentifier);
    }

    private void handleNoTransferIdentifier(User user, MigrationCounters counters) {
        counters.incrementFail();
        log.warn("transfer_identifier 없음 - userId: {}, providerId: {}",
            user.getId(), user.getProviderId());
    }

    private void waitForRateLimit() {
        try {
            Thread.sleep(RATE_LIMIT_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Rate limit 대기 중 인터럽트 발생", e);
        }
    }

    private void handleMigrationFailure(User user, MigrationCounters counters, Exception e) {
        counters.incrementFail();
        log.error("실패 - userId: {}, providerId: {}", user.getId(), user.getProviderId(), e);
    }

    private MigrationResult logAndReturnResult(int total, MigrationCounters counters) {
        MigrationResult result = new MigrationResult(
            total,
            counters.getSuccess(),
            counters.getFail(),
            counters.getSkip()
        );

        log.info("=== 마이그레이션 준비 완료 ===");
        log.info("총: {}, 성공: {}, 실패: {}, 스킵: {}",
            result.total(), result.success(), result.fail(), result.skip());

        return result;
    }

    private static class MigrationCounters {
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failCount = new AtomicInteger(0);
        private final AtomicInteger skipCount = new AtomicInteger(0);

        public void incrementSuccess() {
            successCount.incrementAndGet();
        }

        public void incrementFail() {
            failCount.incrementAndGet();
        }

        public void incrementSkip() {
            skipCount.incrementAndGet();
        }

        public int getSuccess() {
            return successCount.get();
        }

        public int getFail() {
            return failCount.get();
        }

        public int getSkip() {
            return skipCount.get();
        }
    }

    public record MigrationResult(
        int total,
        int success,
        int fail,
        int skip
    ) {}
}
