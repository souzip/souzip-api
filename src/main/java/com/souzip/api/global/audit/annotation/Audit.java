package com.souzip.api.global.audit.annotation;

import com.souzip.api.domain.audit.entity.AuditAction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    AuditAction action();

    String userIdParam() default "";
}
