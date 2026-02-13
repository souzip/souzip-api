package com.souzip.api.domain.admin.model;

import com.souzip.api.domain.admin.exception.AdminErrorCode;
import com.souzip.api.domain.admin.exception.InvalidUsernameException;

public record Username(String value) {

    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 20;

    public Username {
        validateNotBlank(value);
        validateLengthInRange(value);
    }

    private static void validateNotBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUsernameException(AdminErrorCode.INVALID_USERNAME_EMPTY);
        }
    }

    private static void validateLengthInRange(String value) {
        if (isBelowMinLength(value) || isAboveMaxLength(value)) {
            throw new InvalidUsernameException(AdminErrorCode.INVALID_USERNAME_LENGTH);
        }
    }

    private static boolean isBelowMinLength(String value) {
        return value.length() < MIN_LENGTH;
    }

    private static boolean isAboveMaxLength(String value) {
        return value.length() > MAX_LENGTH;
    }
}
