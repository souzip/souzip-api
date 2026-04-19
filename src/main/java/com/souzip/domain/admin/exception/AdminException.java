package com.souzip.domain.admin.exception;

import com.souzip.shared.exception.BusinessException;

public class AdminException extends BusinessException {
    public AdminException(AdminErrorCode errorCode) {
        super(errorCode);
    }
}
