package com.tandiantong.catalog.product;

import java.util.List;

/** 商品创建结果。 */
public record ProductCreationResult(
        ProductProfile product,
        List<ProductSkuProfile> skus,
        List<AddonGroupProfile> addonGroups,
        List<InventoryRecord> inventoryRecords
) {
}
