package com.tandiantong.catalog.product;

import java.util.List;

public record ProductCreationResult(
        ProductProfile product,
        List<ProductSkuProfile> skus,
        List<AddonGroupProfile> addonGroups,
        List<InventoryRecord> inventoryRecords
) {
}
