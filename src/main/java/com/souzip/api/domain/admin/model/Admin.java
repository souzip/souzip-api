package com.souzip.api.domain.admin.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Admin {

    private final UUID id;
    private final Username username;
    private final Password password;
    private final AdminRole role;
    private int loginFailCount;
    private LocalDateTime lockedAt;
    private LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static Admin create(String username,
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
}
