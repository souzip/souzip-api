package com.souzip.global.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse<T> {

    private final T data;
    private final String message;

    @Builder(access = AccessLevel.PRIVATE)
    private SuccessResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public static <T> SuccessResponse<T> of(T data, String message) {
        return SuccessResponse.<T>builder()
            .data(data)
            .message(message)
            .build();
    }

    public static <T> SuccessResponse<T> of(T data) {
        return of(data, "");
    }

    public static SuccessResponse<Void> of(String message) {
        return SuccessResponse.<Void>builder()
            .message(message)
            .build();
    }
}
