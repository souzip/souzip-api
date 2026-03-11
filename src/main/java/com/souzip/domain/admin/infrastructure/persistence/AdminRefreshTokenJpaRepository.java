package com.souzip.domain.admin.infrastructure.persistence;

import com.souzip.domain.admin.infrastructure.entity.AdminRefreshTokenEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRefreshTokenJpaRepository extends JpaRepository<AdminRefreshTokenEntity, UUID> {

    Optional<AdminRefreshTokenEntity> findByToken(String token);

    Optional<AdminRefreshTokenEntity> findByAdminId(UUID adminId);

    int deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}
