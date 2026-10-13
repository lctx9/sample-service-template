package com.example.common.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.common.error.BusinessException;
import com.example.common.error.CommonErrorCode;
import com.example.common.error.ErrorCode;

/**
 * Turns every exception into the common {@link ApiResponse} format.
 * Registered automatically in every service that depends on the common module.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        ErrorCode code = ex.getErrorCode();
        return ResponseEntity.status(code.status()).body(ApiResponse.error(code.code(), ex.getMessage()));
    }

    /** Bean Validation failed (@Valid). Returns which fields are wrong. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        CommonErrorCode code = CommonErrorCode.VALIDATION_ERROR;
        return ResponseEntity.status(code.status()).body(ApiResponse.error(code.code(), code.message(), fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        return toResponse(CommonErrorCode.MALFORMED_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        return toResponse(CommonErrorCode.NOT_FOUND);
    }

    /** Anything we did not expect. Log the details, but never send the stack trace to the client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return toResponse(CommonErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode code) {
        return ResponseEntity.status(code.status()).body(ApiResponse.error(code.code(), code.message()));
    }
}
