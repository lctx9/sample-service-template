package com.example.common.error;

import org.springframework.http.HttpStatus;

/**
 * Every service defines its own error codes as an enum that implements this interface.
 * Example: {@code ItemErrorCode.ITEM_NOT_FOUND}.
 */
public interface ErrorCode {

    /** Stable code the frontend can check, e.g. "ITEM_NOT_FOUND". */
    String code();

    HttpStatus status();

    /** Default message shown to the user. */
    String message();
}
