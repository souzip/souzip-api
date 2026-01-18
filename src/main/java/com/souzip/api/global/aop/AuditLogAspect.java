package com.souzip.api.global.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.souzip.api.domain.audit.entity.AuditLog;
import com.souzip.api.domain.audit.repository.AuditLogRepository;
import com.souzip.api.global.annotation.Audit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Around(
            value = "@annotation(audit)",
            argNames = "joinPoint,audit"
    )
    public Object saveAuditLog(
            ProceedingJoinPoint joinPoint,
            Audit audit
    ) throws Throwable {

        log.info("[AUDIT] ENTER action={}", audit.action());

        Object result = joinPoint.proceed();

        try {
            String userId = extractUserId();
            String requestDto = extractDto(joinPoint.getArgs(), audit.dtoType());
            String requestInfo = extractRequestInfo();

            AuditLog logEntity = AuditLog.of(
                    audit.action(),
                    userId,
                    requestDto,
                    requestInfo
            );

            auditLogRepository.save(logEntity);

            log.info("[AUDIT] SAVED action={}", audit.action());

        } catch (Exception e) {
            log.error("[AUDIT] SAVE FAIL action={}", audit.action(), e);
        }

        return result;
    }

    private String extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    private String extractDto(Object[] args, Class<?> dtoType) {
        if (dtoType == Void.class) return null;

        return Arrays.stream(args)
                .filter(arg -> arg != null && dtoType.isAssignableFrom(arg.getClass()))
                .findFirst()
                .map(arg -> {
                    try {
                        return objectMapper.writeValueAsString(arg);
                    } catch (Exception e) {
                        return "FAIL_TO_SERIALIZE";
                    }
                })
                .orElse(null);
    }

    private String extractRequestInfo() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) return null;

        HttpServletRequest request = attrs.getRequest();
        Map<String, String> info = new HashMap<>();
        info.put("ip", request.getRemoteAddr());
        info.put("userAgent", request.getHeader("User-Agent"));
        info.put("uri", request.getRequestURI());

        return info.toString();
    }
}
