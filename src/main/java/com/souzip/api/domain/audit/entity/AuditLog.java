package com.souzip.api.domain.audit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditActionType action;

    @Column(length = 100)
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String requestDto;

    @Column(columnDefinition = "TEXT")
    private String requestInfo;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public static AuditLog of(
            AuditActionType action,
            String userId,
            String requestDto,
            String requestInfo
    ) {
        AuditLog log = new AuditLog();
        log.action = action;
        log.userId = userId;
        log.requestDto = requestDto;
        log.requestInfo = requestInfo;
        log.timestamp = LocalDateTime.now();
        return log;
    }
}
