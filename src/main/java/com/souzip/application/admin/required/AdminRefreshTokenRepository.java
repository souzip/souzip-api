package com.souzip.application.admin.required;

import com.souzip.domain.admin.AdminRefreshToken;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AdminRefreshTokenRepository extends Repository<AdminRefreshToken, UUID> {
    AdminRefreshToken save(AdminRefreshToken refreshToken);

    Optional<AdminRefreshToken> findByToken(String token);

    Optional<AdminRefreshToken> findByAdminId(UUID adminId);

    void delete(AdminRefreshToken refreshToken);

    int deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}