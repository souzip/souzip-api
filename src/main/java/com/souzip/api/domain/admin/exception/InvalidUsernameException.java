package com.souzip.api.domain.admin.exception;

import com.souzip.api.global.exception.BusinessException;

public class InvalidUsernameException extends BusinessException {

    public InvalidUsernameException(AdminErrorCode errorCode) {
        super(errorCode);
    }
}
