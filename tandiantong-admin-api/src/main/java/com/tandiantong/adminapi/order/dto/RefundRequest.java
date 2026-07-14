package com.tandiantong.adminapi.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 订单退款请求。 */
@Schema(description = "订单整单退款请求")
public record RefundRequest(
        @Schema(description = "退款幂等键，同一订单同一退款动作保持不变", example = "refund-20260714-0001")
        @NotBlank(message = "幂等键不能为空")
        String idempotencyKey,

        @Schema(description = "退款原因", example = "顾客取消取餐")
        @NotBlank(message = "退款原因不能为空")
        String reason
) {
}
