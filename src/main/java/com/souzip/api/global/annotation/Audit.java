package com.souzip.api.global.annotation;

import com.souzip.api.domain.audit.entity.AuditActionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
    AuditActionType action();
    Class<?> dtoType() default Void.class;
}
