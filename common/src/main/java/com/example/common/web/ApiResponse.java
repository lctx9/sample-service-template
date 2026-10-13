package com.example.common.web;

/**
 * Common response format for every API in every service.
 *
 * <pre>
 * Success: { "success": true,  "data": {...}, "errorCode": null,             "message": null }
 * Error:   { "success": false, "data": null,  "errorCode": "ITEM_NOT_FOUND", "message": "Item not found" }
 * </pre>
 */
public record ApiResponse<T>(boolean success, T data, String errorCode, String message) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, errorCode, message);
    }

    /** Error with extra details, e.g. which fields are invalid. */
    public static <T> ApiResponse<T> error(String errorCode, String message, T details) {
        return new ApiResponse<>(false, details, errorCode, message);
    }
}
