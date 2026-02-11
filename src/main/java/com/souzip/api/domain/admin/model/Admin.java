package com.souzip.api.domain.admin.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Admin {

    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    private final UUID id;
    private final Username username;
    private final Password password;
    private final AdminRole role;
    private int loginFailCount;
    private LocalDateTime lockedAt;
    private LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static Admin create(
        String username,
        String rawPassword,
        AdminRole role,
        AdminPasswordEncoder encoder
    ) {
        return new Admin(
            UUID.randomUUID(),
            new Username(username),
            Password.encode(rawPassword, encoder),
            role,
            0, null, null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    public static Admin restore(
        UUID id,
        Username username,
        Password password,
        AdminRole role,
        int loginFailCount,
        LocalDateTime lockedAt,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Admin(id,
            username,
            password,
            role,
            loginFailCount,
            lockedAt,
            lastLoginAt,
            createdAt,
            updatedAt
        );
    }

    public void recordLoginSuccess() {
        this.loginFailCount = 0;
        this.lastLoginAt = LocalDateTime.now();
    }

    public void recordLoginFailure() {
        this.loginFailCount++;
        if (shouldLock()) {
            lock();
        }
    }

    public boolean isLocked() {
        return this.lockedAt != null;
    }

    public boolean matchesPassword(String rawPassword, AdminPasswordEncoder encoder) {
        return this.password.matches(rawPassword, encoder);
    }

    private boolean shouldLock() {
        return this.loginFailCount >= MAX_LOGIN_FAIL_COUNT;
    }

    private void lock() {
        this.lockedAt = LocalDateTime.now();
    }
}
