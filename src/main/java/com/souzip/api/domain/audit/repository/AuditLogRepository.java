package com.souzip.api.domain.audit.repository;

import com.souzip.api.domain.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
