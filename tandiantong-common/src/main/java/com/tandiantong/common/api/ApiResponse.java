package com.tandiantong.common.api;

/** 统一接口响应结构。 */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        String traceId,
        T data,
        String requiredPermission
) {

    public static <T> ApiResponse<T> success(String traceId, T data) {
        return new ApiResponse<>(true, ErrorCode.SUCCESS.code(), ErrorCode.SUCCESS.message(), traceId, data, null);
    }

    public static <T> ApiResponse<T> failure(String traceId, ErrorCode errorCode, String message) {
        return failure(traceId, errorCode, message, null);
    }

    public static <T> ApiResponse<T> failure(String traceId, ErrorCode errorCode, String message, String requiredPermission) {
        return new ApiResponse<>(false, errorCode.code(), message, traceId, null, requiredPermission);
    }
}
