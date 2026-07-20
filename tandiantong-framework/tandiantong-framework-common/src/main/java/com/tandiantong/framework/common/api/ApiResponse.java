package com.tandiantong.framework.common.api;

/** 统一接口响应结构。 */
public class ApiResponse<T> {

    private final boolean success;

    private final String code;

    private final String message;

    private final String traceId;

    private final T data;

    private final String requiredPermission;

    public ApiResponse(boolean success, String code, String message, String traceId, T data, String requiredPermission) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.traceId = traceId;
        this.data = data;
        this.requiredPermission = requiredPermission;
    }

    public static <T> ApiResponse<T> success(String traceId, T data) {
        return new ApiResponse<>(true, ErrorCode.SUCCESS.code(), ErrorCode.SUCCESS.message(), traceId, data, null);
    }

    public static <T> ApiResponse<T> failure(String traceId, ErrorCode errorCode, String message) {
        return failure(traceId, errorCode, message, null);
    }

    public static <T> ApiResponse<T> failure(String traceId, ErrorCode errorCode, String message, String requiredPermission) {
        return new ApiResponse<>(false, errorCode.code(), message, traceId, null, requiredPermission);
    }

    public boolean success() {
        return success;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public String traceId() {
        return traceId;
    }

    public T data() {
        return data;
    }

    public String requiredPermission() {
        return requiredPermission;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getTraceId() {
        return traceId;
    }

    public T getData() {
        return data;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }
}
