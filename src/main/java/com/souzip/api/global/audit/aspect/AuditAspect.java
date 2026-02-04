package com.souzip.api.global.audit.aspect;

import com.souzip.api.domain.audit.entity.AuditAction;
import com.souzip.api.domain.audit.service.AuditService;
import com.souzip.api.domain.auth.dto.LoginResponse;
import com.souzip.api.domain.auth.dto.LoginUserInfo;
import com.souzip.api.domain.user.dto.OnboardingRequest;
import com.souzip.api.domain.user.entity.Provider;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.global.audit.annotation.Audit;
import com.souzip.api.global.audit.dto.AuditContext;
import com.souzip.api.global.util.HttpRequestUtils;
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

    private final AuditService auditService;

    @Around("@annotation(audit)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        AuditAction action = audit.action();

        try {
            Object result = executeMethod(joinPoint);
            logSuccess(request, action, result, joinPoint);
            return result;

        } catch (Exception e) {
            logFailure(request, action, e);
            throw e;
        }
    }

    private Object executeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    private void logSuccess(HttpServletRequest request, AuditAction action,
                            Object result, ProceedingJoinPoint joinPoint) {
        AuditContext context = buildSuccessContext(request, action, result, joinPoint);
        auditService.logSuccess(context);
    }

    private void logFailure(HttpServletRequest request, AuditAction action, Exception e) {
        String ipAddress = HttpRequestUtils.extractClientIp(request);
        String userAgent = HttpRequestUtils.extractUserAgent(request);
        String appVersion = HttpRequestUtils.extractAppVersion(request);
        String failureReason = extractFailureReason(e);

        auditService.logFailure(action, failureReason, ipAddress, userAgent, appVersion);
    }

    private String extractFailureReason(Exception e) {
        return e.getClass().getSimpleName();
    }

    private AuditContext buildSuccessContext(HttpServletRequest request, AuditAction action,
                                             Object result, ProceedingJoinPoint joinPoint) {

        String metadata = buildMetadata(joinPoint);

        return AuditContext.builder()
                .userId(extractUserId(result, joinPoint))
                .action(action)
                .ipAddress(HttpRequestUtils.extractClientIp(request))
                .userAgent(HttpRequestUtils.extractUserAgent(request))
                .appVersion(HttpRequestUtils.extractAppVersion(request))
                .oauthProvider(extractOAuthProvider(joinPoint))
                .metadata(metadata)
                .build();
    }

    private String buildMetadata(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(OnboardingRequest.class::isInstance)
                .map(OnboardingRequest.class::cast)
                .findFirst()
                .map(req -> String.format(
                        "{\"serviceTerms\": %s, \"privacyRequired\": %s, \"marketingConsent\": %s, \"locationService\": %s}",
                        req.serviceTerms(),
                        req.privacyRequired(),
                        req.marketingConsent(),
                        req.locationService()
                ))
                .orElse(null);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = getRequestAttributes();
        return attributes.getRequest();
    }

    private ServletRequestAttributes getRequestAttributes() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        validateAttributes(attributes);

        return attributes;
    }

    private void validateAttributes(ServletRequestAttributes attributes) {
        if (isNull(attributes)) {
            throw new IllegalStateException("No request found");
        }
    }

    private boolean isNull(Object object) {
        return object == null;
    }

    private String extractUserId(Object result, ProceedingJoinPoint joinPoint) {
        String userIdFromArgs = extractUserIdFromArgs(joinPoint);
        if (userIdFromArgs != null) {
            return userIdFromArgs;
        }

        if (isLoginResponse(result)) {
            return extractUserIdFromLoginResponse((LoginResponse) result);
        }

        return extractUserIdFromSecurity();
    }

    private String extractUserIdFromArgs(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .findFirst()
                .orElse(null);
    }

    private boolean isLoginResponse(Object result) {
        return result instanceof LoginResponse;
    }

    private String extractUserIdFromLoginResponse(LoginResponse loginResponse) {
        return Optional.ofNullable(loginResponse.getUser())
                .map(LoginUserInfo::userId)
                .orElse(null);
    }

    private String extractUserIdFromSecurity() {
        return Optional.ofNullable(getAuthentication())
                .filter(this::hasValidPrincipal)
                .map(Authentication::getPrincipal)
                .filter(User.class::isInstance)
                .map(User.class::cast)
                .map(User::getUserId)
                .orElse(null);
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean hasValidPrincipal(Authentication authentication) {
        return isNotNull(authentication.getPrincipal());
    }

    private boolean isNotNull(Object object) {
        return object != null;
    }

    private Provider extractOAuthProvider(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(Provider.class::isInstance)
                .map(Provider.class::cast)
                .findFirst()
                .orElse(null);
    }
}
