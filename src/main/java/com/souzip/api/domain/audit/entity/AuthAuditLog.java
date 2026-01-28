package com.souzip.api.domain.audit.entity;

import com.souzip.api.domain.user.entity.Provider;
import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
    indexes = {
        @Index(name = "idx_audit_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_attempt_time", columnList = "attempt_time"),
        @Index(name = "idx_audit_ip_address", columnList = "ip_address"),
        @Index(name = "idx_audit_success", columnList = "login_success, attempt_time")
    }
)
public class AuthAuditLog extends BaseEntity {

    private String userId;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(nullable = false)
    private Boolean loginSuccess;

    private String failureReason;

    @Column(nullable = false)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    private String appVersion;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime attemptTime;

    public static AuthAuditLog success(
        String userId,
        Provider provider,
        String ipAddress,
        String userAgent,
        String appVersion
    ) {
        return AuthAuditLog.builder()
            .userId(userId)
            .provider(provider)
            .loginSuccess(true)
            .ipAddress(ipAddress)
            .deviceType(DeviceType.fromUserAgent(userAgent))
            .appVersion(appVersion)
            .userAgent(userAgent)
            .build();
    }

    public static AuthAuditLog failure(
        Provider provider,
        String failureReason,
        String ipAddress,
        String userAgent,
        String appVersion
    ) {
        return AuthAuditLog.builder()
            .provider(provider)
            .loginSuccess(false)
            .failureReason(failureReason)
            .ipAddress(ipAddress)
            .deviceType(DeviceType.fromUserAgent(userAgent))
            .appVersion(appVersion)
            .userAgent(userAgent)
            .build();
    }
}
