package com.souzip.api.domain.admin.exception;

import com.souzip.api.global.exception.BusinessException;

public class AdminLockedException extends BusinessException {

    public AdminLockedException() {
        super(AdminErrorCode.ADMIN_LOCKED);
    }
}
