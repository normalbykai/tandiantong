package com.tandiantong.adminapi.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/** 创建商品 SKU 请求。 */
@Schema(description = "创建商品 SKU 请求")
public record CreateSkuRequest(
        @Schema(description = "SKU 规格描述", example = "大份")
        @NotBlank(message = "SKU规格不能为空")
        String specificationText,

        @Schema(description = "商户自定义 SKU 编码", example = "BEEF-RICE-L")
        @NotBlank(message = "SKU编码不能为空")
        String skuCode,

        @Schema(description = "SKU 成交单价，单位为分", example = "2200")
        @PositiveOrZero(message = "SKU价格不能小于零")
        int priceCent,

        @Schema(description = "初始可售库存数量", example = "100")
        @PositiveOrZero(message = "初始库存不能小于零")
        int initialStock,

        @Schema(description = "库存预警阈值", example = "10")
        @PositiveOrZero(message = "预警库存不能小于零")
        int warningStock
) {
}
