package com.example.common.error;

/**
 * Throw this when a business rule is broken. The global handler turns it into an
 * {@link com.example.common.web.ApiResponse} with the right HTTP status.
 *
 * <pre>
 * throw new BusinessException(ItemErrorCode.ITEM_NOT_FOUND);
 * </pre>
 */
public class BusinessException extends RuntimeException {

    private final transient ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
