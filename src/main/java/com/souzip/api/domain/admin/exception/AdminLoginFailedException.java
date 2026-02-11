package com.souzip.api.domain.admin.exception;

import com.souzip.api.global.exception.BusinessException;

public class AdminLoginFailedException extends BusinessException {

    public AdminLoginFailedException() {
        super(AdminErrorCode.ADMIN_LOGIN_FAILED);
    }
}
