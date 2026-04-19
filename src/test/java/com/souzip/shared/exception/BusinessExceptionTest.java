package com.souzip.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @DisplayName("ErrorCode로 예외 생성")
    @Test
    void createWithErrorCode() {
        // given
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;

        // when
        BusinessException exception = new BusinessException(errorCode);

        // then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorCode.getMessage(), exception.getMessage());
    }

    @DisplayName("ErrorCode와 메시지로 예외 생성")
    @Test
    void createWithErrorCodeAndCustomMessage() {
        // given
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        String customMessage = "사용자 정의 오류 메시지";

        // when
        BusinessException exception = new BusinessException(errorCode, customMessage);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
    }
}
