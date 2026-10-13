package com.example.common.error;

import org.springframework.http.HttpStatus;

/**
 * Error codes shared by all services.
 */
public enum CommonErrorCode implements ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Invalid input data"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Request body is missing or not valid JSON"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Login required"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "You do not have permission to do this"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, please try again later");

    private final HttpStatus status;
    private final String message;

    CommonErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public String code() {
        return name();
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }
}
