package com.souzip.api.domain.admin.infrastructure.persistence;

import com.souzip.api.domain.admin.infrastructure.entity.AdminEntity;
import com.souzip.api.domain.admin.model.AdminRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminJpaRepository extends JpaRepository<AdminEntity, UUID> {

    Optional<AdminEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<AdminEntity> findByRoleNot(AdminRole role, Pageable pageable);

    long countByRoleNot(AdminRole role);
}
