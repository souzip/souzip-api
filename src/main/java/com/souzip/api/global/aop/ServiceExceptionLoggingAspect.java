package com.souzip.api.global.aop;

import com.albafit.api.global.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(2)
public class ServiceExceptionLoggingAspect {

    @Pointcut("execution(* com.albafit.api..service..*(..))")
    public void serviceLayer() {}

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logServiceException(JoinPoint joinPoint, Exception ex) {
        if (ex instanceof BusinessException) return;

        log.error("[SERVICE-ERROR] method={} message={}",
            joinPoint.getSignature().toShortString(),
            ex.getMessage(),
            ex
        );
    }
}
