package com.tandiantong.catalog.product;

import java.util.List;

public record SkuDraft(
        List<SpecificationValue> specifications,
        int priceCent,
        int initialStock,
        int warningStock,
        String skuCode
) {
}
