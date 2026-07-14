package com.tandiantong.adminapi.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

/** 创建商品请求。 */
@Schema(description = "创建商品和 SKU 请求")
public record CreateProductRequest(
        @Schema(description = "商品名称", example = "招牌牛肉饭")
        @NotBlank(message = "商品名称不能为空")
        String productName,

        @Schema(description = "商品分类名称", example = "主食")
        @NotBlank(message = "商品分类不能为空")
        String categoryName,

        @Schema(description = "商品基础价格，单位为分", example = "1800")
        @PositiveOrZero(message = "商品价格不能小于零")
        int basePriceCent,

        @Schema(description = "创建后是否立即上架", example = "true")
        boolean onShelf,

        @Schema(description = "商品 SKU 列表，至少包含一个 SKU")
        @Valid
        @NotEmpty(message = "至少需要一个SKU")
        List<CreateSkuRequest> skus
) {
}
