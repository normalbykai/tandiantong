package com.tandiantong.common.api;

/** 系统统一业务错误码。 */
public enum ErrorCode {
    /** 请求处理成功。 */
    SUCCESS("SUCCESS", "处理成功"),
    /** 请求参数或业务入参校验失败。 */
    VALIDATION_FAILED("COMMON_VALIDATION_FAILED", "参数校验失败"),
    /** 登录状态缺失、无效或已过期。 */
    UNAUTHORIZED("COMMON_UNAUTHORIZED", "登录状态无效或已过期"),
    /** 当前账号没有访问目标资源或操作的权限。 */
    FORBIDDEN("COMMON_FORBIDDEN", "当前账号没有操作权限"),
    /** 请求访问的业务资源不存在。 */
    RESOURCE_NOT_FOUND("COMMON_RESOURCE_NOT_FOUND", "资源不存在"),
    /** 系统内部异常或暂时不可用。 */
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
