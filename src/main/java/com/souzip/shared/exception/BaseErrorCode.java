package com.souzip.shared.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
    HttpStatus getStatus();

    String getMessage();

    String name();
}
