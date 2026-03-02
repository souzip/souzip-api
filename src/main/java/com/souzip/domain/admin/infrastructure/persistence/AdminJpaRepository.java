package com.souzip.domain.admin.infrastructure.persistence;

import com.souzip.domain.admin.infrastructure.entity.AdminEntity;
import com.souzip.domain.admin.model.AdminRole;
import java.util.List;
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

    List<AdminEntity> findAllByIdIn(List<UUID> ids);
}
