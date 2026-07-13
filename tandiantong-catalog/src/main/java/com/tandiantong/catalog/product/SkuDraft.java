package com.tandiantong.catalog.product;

import java.util.List;

/** 待创建 SKU 草稿。 */
public record SkuDraft(
        List<SpecificationValue> specifications,
        int priceCent,
        int initialStock,
        int warningStock,
        String skuCode
) {
}
