package com.souzip.api.domain.admin.exception;

import com.souzip.api.global.exception.BusinessException;

public class AdminException extends BusinessException {
    public AdminException(AdminErrorCode errorCode) {
        super(errorCode);
    }
}
