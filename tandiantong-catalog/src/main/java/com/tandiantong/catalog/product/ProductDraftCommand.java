package com.tandiantong.catalog.product;

import java.util.List;

public record ProductDraftCommand(
        String productName,
        String categoryName,
        int basePriceCent,
        PaymentConfigStatus paymentConfigStatus,
        boolean publishNow,
        List<SkuDraft> skus,
        List<AddonGroupDraft> addonGroups
) {
}
