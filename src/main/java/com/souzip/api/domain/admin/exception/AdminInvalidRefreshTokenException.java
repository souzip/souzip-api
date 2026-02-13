package com.souzip.api.domain.admin.exception;

import com.souzip.api.global.exception.BusinessException;

public class AdminInvalidRefreshTokenException extends BusinessException {

    public AdminInvalidRefreshTokenException() {
        super(AdminErrorCode.INVALID_REFRESH_TOKEN);
    }
}
