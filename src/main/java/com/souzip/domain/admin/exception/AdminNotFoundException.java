package com.souzip.domain.admin.exception;

import com.souzip.shared.exception.BusinessException;

public class AdminNotFoundException extends BusinessException {

    public AdminNotFoundException() {
        super(AdminErrorCode.ADMIN_NOT_FOUND);
    }
}
