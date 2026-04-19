package com.souzip.domain.admin.exception;

import com.souzip.shared.exception.BusinessException;

public class AdminExpiredRefreshTokenException extends BusinessException {

    public AdminExpiredRefreshTokenException() {
        super(AdminErrorCode.EXPIRED_REFRESH_TOKEN);
    }
}
