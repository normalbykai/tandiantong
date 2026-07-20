package com.tandiantong.framework.common.exception;

import com.tandiantong.framework.common.api.ErrorCode;

/** 携带稳定错误码和中文消息的业务异常。 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
