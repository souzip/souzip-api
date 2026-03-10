package com.souzip.adapter.security.admin.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('SUPER_ADMIN')")
public @interface SuperAdminOnly {
}
