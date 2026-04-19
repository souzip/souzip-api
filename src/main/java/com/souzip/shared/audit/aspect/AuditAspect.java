package com.souzip.shared.audit.aspect;

import com.souzip.auth.application.dto.LoginInfo;
import com.souzip.domain.audit.entity.AuditAction;
import com.souzip.domain.audit.service.AuditService;
import com.souzip.domain.shared.Provider;
import com.souzip.domain.user.dto.OnboardingRequest;
import com.souzip.shared.audit.annotation.Audit;
import com.souzip.shared.audit.dto.AuditContext;
import com.souzip.shared.exception.BusinessException;
import com.souzip.shared.exception.ErrorCode;
import com.souzip.shared.util.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class AuditAspect {

    private static final String ONBOARDING_METADATA_FORMAT =
            "{\"serviceTerms\": %s, \"privacyRequired\": %s, \"marketingConsent\": %s, \"locationService\": %s}";

    private final AuditService auditService;

    @Around("@annotation(audit)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        AuditAction action = audit.action();

        try {
            Object result = joinPoint.proceed();
            logSuccess(request, action, result, joinPoint, audit);
            return result;
        } catch (Exception e) {
            logFailure(request, action, e);
            throw e;
        }
    }

    private void logSuccess(HttpServletRequest request,
                            AuditAction action,
                            Object result,
                            ProceedingJoinPoint joinPoint,
                            Audit audit
    ) {
        AuditContext context = buildSuccessContext(request, action, result, joinPoint, audit);
        auditService.logSuccess(context);
    }

    private void logFailure(HttpServletRequest request, AuditAction action, Exception e) {
        auditService.logFailure(
                action,
                e.getClass().getSimpleName(),
                HttpRequestUtils.extractClientIp(request),
                HttpRequestUtils.extractUserAgent(request),
                HttpRequestUtils.extractAppVersion(request)
        );
    }

    private AuditContext buildSuccessContext(HttpServletRequest request,
                                             AuditAction action,
                                             Object result,
                                             ProceedingJoinPoint joinPoint,
                                             Audit audit
    ) {
        return AuditContext.builder()
                .userId(extractUserId(result, joinPoint, audit))
                .action(action)
                .ipAddress(HttpRequestUtils.extractClientIp(request))
                .userAgent(HttpRequestUtils.extractUserAgent(request))
                .appVersion(HttpRequestUtils.extractAppVersion(request))
                .oauthProvider(extractOAuthProvider(joinPoint))
                .metadata(buildMetadata(joinPoint))
                .build();
    }

    private String extractUserId(Object result, ProceedingJoinPoint joinPoint, Audit audit) {
        if (hasUserIdParam(audit)) {
            return extractUserIdFromArgs(joinPoint);
        }

        if (result instanceof LoginInfo loginInfo) {
            return loginInfo.userId();
        }

        return extractUserIdFromSecurity();
    }

    private boolean hasUserIdParam(Audit audit) {
        return !audit.userIdParam().isEmpty();
    }

    private String extractUserIdFromArgs(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(this::isUserIdArg)
                .map(String.class::cast)
                .findFirst()
                .orElse(null);
    }

    private String extractUserIdFromSecurity() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(Long.class::isInstance)
                .map(principal -> String.valueOf((Long) principal))
                .orElse(null);
    }

    private Provider extractOAuthProvider(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(Provider.class::isInstance)
                .map(Provider.class::cast)
                .findFirst()
                .orElse(null);
    }

    private String buildMetadata(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(OnboardingRequest.class::isInstance)
                .map(OnboardingRequest.class::cast)
                .findFirst()
                .map(this::formatOnboardingMetadata)
                .orElse(null);
    }

    private String formatOnboardingMetadata(OnboardingRequest req) {
        return String.format(
                ONBOARDING_METADATA_FORMAT,
                req.serviceTerms(),
                req.privacyRequired(),
                req.marketingConsent(),
                req.locationService()
        );
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        validateRequestAttributes(attributes);
        return attributes.getRequest();
    }

    private void validateRequestAttributes(ServletRequestAttributes attributes) {
        if (isMissingAttributes(attributes)) {
            throw new BusinessException(ErrorCode.HTTP_REQUEST_CONTEXT_NOT_FOUND);
        }
    }

    private boolean isUserIdArg(Object arg) {
        return arg instanceof String;
    }

    private boolean isMissingAttributes(ServletRequestAttributes attributes) {
        return attributes == null;
    }
}