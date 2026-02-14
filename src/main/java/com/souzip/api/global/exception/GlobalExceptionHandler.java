package com.souzip.api.global.exception;

import com.souzip.api.global.aop.MdcTraceId;
import com.souzip.api.global.common.dto.ErrorResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ensureTraceId();

        log.error("[BUSINESS-ERROR] errorCode={} status={} message={}",
            e.getErrorCode().name(),
            e.getErrorCode().getStatus(),
            e.getMessage()
        );

        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(ErrorResponse.of(e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ensureTraceId();

        log.error("[ILLEGAL-ARGUMENT-ERROR] message={}", e.getMessage(), e);

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getStatus())
            .body(ErrorResponse.of(e.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request
    ) {
        ensureTraceId();

        List<ErrorResponse.FieldError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.of(
                error.getField(),
                Optional.ofNullable(error.getRejectedValue()).map(Object::toString).orElse(""),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());

        log.error("[VALIDATION-ERROR] errors={}", errors);

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getStatus())
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT.getMessage(), errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        ensureTraceId();

        log.error("[UNEXPECTED-ERROR] type={} message={}", e.getClass().getSimpleName(), e.getMessage());

        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        ensureTraceId();

        log.error("[ACCESS-DENIED-ERROR] message={}", e.getMessage());

        return ResponseEntity
            .status(ErrorCode.FORBIDDEN.getStatus())
            .body(ErrorResponse.of(ErrorCode.FORBIDDEN.getMessage()));
    }

    private void ensureTraceId() {
        MdcTraceId.putIfAbsent();
    }
}
