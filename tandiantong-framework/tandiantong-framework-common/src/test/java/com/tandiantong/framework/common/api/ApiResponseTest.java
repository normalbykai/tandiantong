package com.tandiantong.framework.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.tandiantong.framework.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void successShouldContainStableCodeAndTraceId() {
        ApiResponse<String> response = ApiResponse.success("trace-001", "已完成");

        assertThat(response.success()).isTrue();
        assertThat(response.code()).isEqualTo(ErrorCode.SUCCESS.code());
        assertThat(response.message()).isEqualTo("处理成功");
        assertThat(response.traceId()).isEqualTo("trace-001");
        assertThat(response.data()).isEqualTo("已完成");
    }

    @Test
    void failureShouldUseBusinessErrorCodeAndChineseMessage() {
        ApiResponse<Void> response = ApiResponse.failure("trace-002", ErrorCode.VALIDATION_FAILED, "手机号格式不正确");

        assertThat(response.success()).isFalse();
        assertThat(response.code()).isEqualTo("COMMON_VALIDATION_FAILED");
        assertThat(response.message()).isEqualTo("手机号格式不正确");
        assertThat(response.traceId()).isEqualTo("trace-002");
        assertThat(response.data()).isNull();
    }

    @Test
    void businessExceptionShouldExposeOnlyStableBusinessFields() {
        BusinessException exception = new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");

        assertThat(exception.errorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("订单不存在");
    }
}
