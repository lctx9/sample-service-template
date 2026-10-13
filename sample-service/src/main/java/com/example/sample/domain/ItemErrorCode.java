package com.example.sample.domain;

import org.springframework.http.HttpStatus;

import com.example.common.error.ErrorCode;

/**
 * Error codes of this service. Name them as {@code <THING>_<PROBLEM>}.
 */
public enum ItemErrorCode implements ErrorCode {

    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Item not found"),
    ITEM_NAME_DUPLICATED(HttpStatus.CONFLICT, "Item name is already used");

    private final HttpStatus status;
    private final String message;

    ItemErrorCode(HttpStatus status, String message) {
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
