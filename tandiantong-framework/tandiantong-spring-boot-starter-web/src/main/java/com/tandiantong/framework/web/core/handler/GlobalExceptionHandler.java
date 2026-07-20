package com.tandiantong.framework.web.core.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.tandiantong.framework.common.api.ApiResponse;
import com.tandiantong.framework.common.api.ErrorCode;
import com.tandiantong.framework.common.exception.BusinessException;
import com.tandiantong.framework.web.core.util.TraceIdHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 全局接口异常处理器，统一转换业务错误和参数校验错误。 */
@RestControllerAdvice(basePackages = "com.tandiantong")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        log.warn("业务请求未通过校验，错误码：{}，原因：{}", exception.errorCode().code(), exception.getMessage());
        return ResponseEntity.status(statusOf(exception.errorCode()))
                .body(ApiResponse.failure(TraceIdHolder.get(), exception.errorCode(), exception.getMessage()));
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotLoginException(NotLoginException exception) {
        log.warn("请求未登录或登录状态已过期，原因：{}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure(TraceIdHolder.get(), ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.message()));
    }

    /** 将 Sa-Token 原生权限不足异常转换为统一的业务响应。 */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotPermissionException(NotPermissionException exception) {
        log.warn("当前账号权限不足，所需权限：{}", exception.getPermission());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(TraceIdHolder.get(), ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.message(), exception.getPermission()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("系统处理请求失败，已隐藏内部异常细节", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(TraceIdHolder.get(), ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.message()));
    }

    private HttpStatus statusOf(ErrorCode errorCode) {
        return switch (errorCode) {
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;
            case SUCCESS, INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
