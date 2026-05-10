package com.souzip.domain.admin.exception;

import com.souzip.shared.exception.BusinessException;

public class AdminInvalidRefreshTokenException extends BusinessException {

    public AdminInvalidRefreshTokenException() {
        super(AdminErrorCode.INVALID_REFRESH_TOKEN);
    }
}
