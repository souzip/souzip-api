package com.souzip.api.domain.admin.model;

import com.souzip.api.domain.admin.exception.AdminErrorCode;
import com.souzip.api.domain.admin.exception.InvalidUsernameException;

public record Username(String value) {

    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 10;

    public Username {
        validateNotBlank(value);
        validateLengthInRange(value);
    }


    private static void validateNotBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUsernameException(AdminErrorCode.INVALID_USERNAME);
        }
    }

    private static void validateLengthInRange(String value) {
        if (isTooShort(value) || isTooLong(value)) {
            throw new InvalidUsernameException(AdminErrorCode.INVALID_USERNAME);
        }
    }

    private static boolean isTooShort(String value) {
        return value.length() < MIN_LENGTH;
    }

    private static boolean isTooLong(String value) {
        return value.length() > MAX_LENGTH;
    }
}
