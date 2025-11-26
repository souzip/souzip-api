package com.souzip.api.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(1)
public class TraceIdAspect {

    @Around("execution(* com.souzip.api..controller..*(..)) || execution(* com.souzip.api..service..*(..))")
    public Object setTraceId(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean isNewTrace = MdcTraceId.putIfAbsent();
        try {
            return joinPoint.proceed();
        } finally {
            if (isNewTrace) {
                MdcTraceId.remove();
            }
        }
    }
}
