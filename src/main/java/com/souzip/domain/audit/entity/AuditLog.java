package com.souzip.domain.audit.entity;

import com.souzip.shared.domain.BaseEntity;
import com.souzip.shared.domain.Provider;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
        indexes = {
                @Index(name = "idx_audit_user_id", columnList = "user_id"),
                @Index(name = "idx_audit_action", columnList = "action, created_at"),
                @Index(name = "idx_audit_category", columnList = "category, created_at"),
                @Index(name = "idx_audit_target", columnList = "target_type, target_id"),
                @Index(name = "idx_audit_created_at", columnList = "created_at DESC")
        }
)
public class AuditLog extends BaseEntity {

    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditCategory category;

    @Column(nullable = false)
    private Boolean success;

    private String failureReason;

    private String targetType;

    private Long targetId;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(nullable = false)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    private String appVersion;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    public static AuditLog success(
            String userId,
            AuditAction action,
            String ipAddress,
            String userAgent,
            String appVersion,
            String metadata
    ) {
        return AuditLog.builder()
                .userId(userId)
                .action(action)
                .category(action.getCategory())
                .success(true)
                .ipAddress(ipAddress)
                .deviceType(DeviceType.fromUserAgent(userAgent))
                .appVersion(appVersion)
                .userAgent(userAgent)
                .metadata(metadata)
                .build();
    }

    public static AuditLog failure(
            AuditAction action,
            String failureReason,
            String ipAddress,
            String userAgent,
            String appVersion,
            String metadata
    ) {
        return AuditLog.builder()
                .action(action)
                .category(action.getCategory())
                .success(false)
                .failureReason(failureReason)
                .ipAddress(ipAddress)
                .deviceType(DeviceType.fromUserAgent(userAgent))
                .appVersion(appVersion)
                .userAgent(userAgent)
                .metadata(metadata)
                .build();
    }

    public void setOAuthProvider(Provider provider) {
        this.provider = provider;
    }

    public void setTarget(String targetType, Long targetId) {
        this.targetType = targetType;
        this.targetId = targetId;
    }
}
