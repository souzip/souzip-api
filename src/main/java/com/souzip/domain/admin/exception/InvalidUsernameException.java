package com.souzip.domain.admin.exception;

import com.souzip.shared.exception.BusinessException;

public class InvalidUsernameException extends BusinessException {

    public InvalidUsernameException(AdminErrorCode errorCode) {
        super(errorCode);
    }
}
