package com.souzip.api.domain.admin.infrastructure.persistence;

import com.souzip.api.domain.admin.infrastructure.entity.AdminEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminJpaRepository extends JpaRepository<AdminEntity, UUID> {

    Optional<AdminEntity> findByUsername(String username);

    int deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}
