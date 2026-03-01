package com.souzip.domain.admin.repository;

import com.souzip.domain.admin.model.AdminRefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AdminRefreshTokenRepository {

    AdminRefreshToken save(AdminRefreshToken refreshToken);

    Optional<AdminRefreshToken> findByToken(String token);

    Optional<AdminRefreshToken> findByAdminId(UUID adminId);

    void delete(AdminRefreshToken refreshToken);

    int deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}
