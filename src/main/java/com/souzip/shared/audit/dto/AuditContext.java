package com.souzip.shared.audit.dto;

import com.souzip.domain.audit.entity.AuditAction;
import com.souzip.domain.shared.Provider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditContext {

    private final String userId;
    private final AuditAction action;
    private final String ipAddress;
    private final String userAgent;
    private final String appVersion;
    private final Provider oauthProvider;
    private final String targetType;
    private final Long targetId;
    private final String metadata;

    public boolean hasOAuthProvider() {
        return oauthProvider != null;
    }

    public boolean hasTarget() {
        return targetType != null && targetId != null;
    }
}
