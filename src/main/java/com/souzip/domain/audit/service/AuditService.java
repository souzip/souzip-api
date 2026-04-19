package com.souzip.domain.audit.service;

import com.souzip.domain.audit.entity.AuditAction;
import com.souzip.domain.audit.entity.AuditLog;
import com.souzip.domain.audit.repository.AuditLogRepository;
import com.souzip.shared.audit.dto.AuditContext;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuditService {

    private static final String METRIC_NAME = "audit_total";
    private static final String TAG_ACTION = "action";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_SUCCESS = "success";

    private final AuditLogRepository repository;
    private final MeterRegistry meterRegistry;

    @Async("auditLogExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuccess(AuditContext context) {
        try {
            AuditLog auditLog = createSuccessLog(context);
            saveLog(auditLog);
            incrementSuccessMetric(context.getAction());
            logSuccessInfo(context);

        } catch (Exception e) {
            logSaveError(e);
        }
    }

    @Async("auditLogExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(AuditAction action, String failureReason,
                           String ipAddress, String userAgent, String appVersion) {
        try {
            AuditLog auditLog = createFailureLog(action, failureReason,
                    ipAddress, userAgent, appVersion);
            saveLog(auditLog);
            incrementFailureMetric(action);
            logFailureWarning(action, failureReason);

        } catch (Exception e) {
            logSaveError(e);
        }
    }

    private AuditLog createSuccessLog(AuditContext context) {
        AuditLog auditLog = AuditLog.success(
                context.getUserId(),
                context.getAction(),
                context.getIpAddress(),
                context.getUserAgent(),
                context.getAppVersion(),
                context.getMetadata()
        );

        enrichLogWithOptionalFields(auditLog, context);

        return auditLog;
    }

    private void enrichLogWithOptionalFields(AuditLog auditLog, AuditContext context) {
        if (context.hasOAuthProvider()) {
            auditLog.setOAuthProvider(context.getOauthProvider());
        }

        if (context.hasTarget()) {
            auditLog.setTarget(context.getTargetType(), context.getTargetId());
        }
    }

    private AuditLog createFailureLog(AuditAction action, String failureReason,
                                      String ipAddress, String userAgent, String appVersion) {
        return AuditLog.failure(
                action,
                failureReason,
                ipAddress,
                userAgent,
                appVersion,
                null
        );
    }

    private void saveLog(AuditLog auditLog) {
        repository.save(auditLog);
    }

    private void incrementSuccessMetric(AuditAction action) {
        incrementMetric(action, true);
    }

    private void incrementFailureMetric(AuditAction action) {
        incrementMetric(action, false);
    }

    private void incrementMetric(AuditAction action, boolean success) {
        buildCounter(action, success).increment();
    }

    private Counter buildCounter(AuditAction action, boolean success) {
        return meterRegistry.counter(
                METRIC_NAME,
                TAG_ACTION, action.name(),
                TAG_CATEGORY, action.getCategory().name(),
                TAG_SUCCESS, String.valueOf(success)
        );
    }

    private void logSuccessInfo(AuditContext context) {
        log.info("[Audit success] - userId: {}, action: {}, category: {}",
                context.getUserId(),
                context.getAction(),
                context.getAction().getCategory());
    }

    private void logFailureWarning(AuditAction action, String failureReason) {
        log.warn("[Audit failure] - action: {}, reason: {}",
                action,
                failureReason);
    }

    private void logSaveError(Exception e) {
        log.error("로그 저장에 실패했습니다.", e);
    }
}
