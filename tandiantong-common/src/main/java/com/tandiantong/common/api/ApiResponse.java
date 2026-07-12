package com.tandiantong.common.api;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        String traceId,
        T data
) {

    public static <T> ApiResponse<T> success(String traceId, T data) {
        return new ApiResponse<>(true, ErrorCode.SUCCESS.code(), ErrorCode.SUCCESS.message(), traceId, data);
    }

    public static <T> ApiResponse<T> failure(String traceId, ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, errorCode.code(), message, traceId, null);
    }
}
