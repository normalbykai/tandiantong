package com.tandiantong.bootstrap.web;

import cn.dev33.satoken.exception.NotLoginException;
import com.tandiantong.common.api.ApiResponse;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
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
