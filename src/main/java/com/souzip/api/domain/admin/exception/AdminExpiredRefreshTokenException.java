package com.souzip.api.domain.admin.exception;

import com.souzip.api.global.exception.BusinessException;

public class AdminExpiredRefreshTokenException extends BusinessException {

    public AdminExpiredRefreshTokenException() {
        super(AdminErrorCode.EXPIRED_REFRESH_TOKEN);
    }
}
