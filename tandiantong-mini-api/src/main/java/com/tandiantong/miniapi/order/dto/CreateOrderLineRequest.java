package com.tandiantong.miniapi.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** 创建订单商品明细请求。 */
@Schema(description = "创建订单商品明细请求")
public record CreateOrderLineRequest(
        @Schema(description = "商品 SKU ID", example = "1")
        @NotNull(message = "SKU不能为空")
        Long skuId,

        @Schema(description = "购买数量", example = "2")
        @Positive(message = "商品数量必须大于零")
        int quantity
) {
}
