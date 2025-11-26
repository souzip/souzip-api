package com.souzip.api.global.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String message;
    private final List<FieldError> errors;

    @Builder(access = AccessLevel.PRIVATE)
    private ErrorResponse(String traceId, String message, List<FieldError> errors) {
        this.message = message;
        this.errors = errors;
    }

    public static ErrorResponse of(String message) {
        return ErrorResponse.builder()
            .message(message)
            .build();
    }

    public static ErrorResponse of(String message, List<FieldError> errors) {
        return ErrorResponse.builder()
            .message(message)
            .errors(errors)
            .build();
    }

    @Getter
    public static class FieldError {

        private final String field;
        private final String value;
        private final String reason;

        @Builder(access = AccessLevel.PRIVATE)
        private FieldError(String field, String value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

        public static FieldError of(String field, String value, String reason) {
            return FieldError.builder()
                .field(field)
                .value(value)
                .reason(reason)
                .build();
        }
    }
}
