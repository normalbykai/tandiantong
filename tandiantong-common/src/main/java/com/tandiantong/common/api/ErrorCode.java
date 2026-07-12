package com.tandiantong.common.api;

public enum ErrorCode {
    SUCCESS("SUCCESS", "处理成功"),
    VALIDATION_FAILED("COMMON_VALIDATION_FAILED", "参数校验失败"),
    RESOURCE_NOT_FOUND("COMMON_RESOURCE_NOT_FOUND", "资源不存在"),
    INTERNAL_ERROR("COMMON_INTERNAL_ERROR", "系统繁忙，请稍后再试");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
