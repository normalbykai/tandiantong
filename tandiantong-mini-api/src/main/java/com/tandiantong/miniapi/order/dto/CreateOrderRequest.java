package com.tandiantong.miniapi.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** 创建商品订单请求。 */
@Schema(description = "小程序创建商品订单请求")
public record CreateOrderRequest(
        @Schema(description = "商户小程序入口码", example = "scene_xinghe_001")
        @NotBlank(message = "商户入口码不能为空")
        String sceneKey,

        @Schema(description = "下单幂等键，同一次提交重试时保持不变", example = "order-20260714-0001")
        @NotBlank(message = "幂等键不能为空")
        String idempotencyKey,

        @Schema(description = "顾客联系电话", example = "13800000000")
        @NotBlank(message = "联系电话不能为空")
        String contactMobile,

        @Schema(description = "顾客期望取餐时间文本", example = "今天 18:30")
        @NotBlank(message = "取餐时间不能为空")
        String pickupTimeText,

        @Schema(description = "订单商品明细，至少包含一项")
        @Valid
        @NotEmpty(message = "订单至少需要一个商品")
        List<CreateOrderLineRequest> lines
) {
}
