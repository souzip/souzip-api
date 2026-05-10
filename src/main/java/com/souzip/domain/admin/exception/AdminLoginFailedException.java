package com.souzip.domain.admin.exception;

import com.souzip.shared.exception.BusinessException;

public class AdminLoginFailedException extends BusinessException {

    public AdminLoginFailedException() {
        super(AdminErrorCode.ADMIN_LOGIN_FAILED);
    }
}
